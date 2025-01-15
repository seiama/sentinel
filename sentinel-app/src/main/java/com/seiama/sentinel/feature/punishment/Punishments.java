package com.seiama.sentinel.feature.punishment;

import com.seiama.sentinel.command.Options;
import com.seiama.sentinel.common.Listener;
import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.common.model.PunishmentRepository;
import com.seiama.sentinel.feature.punishment.display.PunishmentDisplay;
import com.seiama.sentinel.feature.punishment.display.PunishmentDisplayStyle;
import com.seiama.sentinel.feature.punishment.display.PunishmentMessages;
import com.seiama.sentinel.feature.punishment.predicate.CanPunish;
import com.seiama.sentinel.reactive.Reactive;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.BanQuerySpec;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.http.client.ClientException;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class Punishments implements Listener {
  private static final boolean ACTUALLY_APPLY_PUNISHMENT = true;
  private final GuildRepository guilds;
  private final PunishmentRepository punishments;

  @Autowired
  private Punishments(final GuildRepository guilds, final PunishmentRepository punishments) {
    this.guilds = guilds;
    this.punishments = punishments;
  }

  @Override
  public @NotNull Mono<Void> listen(final @NotNull GatewayDiscordClient client) {
    return Mono.when(
      client.on(MemberJoinEvent.class, event -> {
        final Member member = event.getMember();
        return this.punishments.findAllByGuildAndPunishedIdAndTypeAndStaleIsNotOrderByDateDesc(event.getGuildId(), member.getId(), PunishmentModel.Type.BAN, true)
          .next()
          .flatMap(punishment -> member.ban(
            BanQuerySpec.builder()
              .reason("Enforcing punishment %s".formatted(punishment._id()))
              .build()
          ).onErrorResume(ClientException.class, e -> member.kick("Enforcing punishment %s".formatted(punishment._id()))));
      })
    );
  }

  public @NotNull Mono<?> command(
    final ChatInputInteractionEvent event,
    final Guild guild,
    final PunishmentModel.Type type,
    final PunishmentAction action
  ) {
    final Interaction interaction = event.getInteraction();
    final Member punisher = interaction.getMember().orElseThrow();
    return event.deferReply().then(
      Options.user(event, Options.MEMBER)
        .orElse(Mono.empty())
        .filterWhen(new CanPunish<>(this.guilds, guild, punisher))
        .switchIfEmpty(event.editReply().withContentOrNull(PunishmentMessages.mayNotPunish()).then(Mono.empty()))
        .flatMap(punished -> this.create(
          guild,
          punisher,
          punished,
          type,
          action,
          Options.string(event, Options.REASON).orElse(null),
          false
        ))
        .flatMap(punishment -> event.editReply().withContent(Possible.of(Optional.of(PunishmentMessages.punishmentPunisherResponse(punishment)))))
    );
  }

  public @NotNull Mono<PunishmentModel.Complete> create(
    final Guild guild,
    final User punisher,
    final User punished,
    final PunishmentModel.Type type,
    final PunishmentAction action,
    final @Nullable String reason,
    final boolean automatic
  ) {
    return this.punishments
      .insert(PunishmentModel.Complete.create(
        guild.getId(),
        type,
        Instant.now(),
        punisher,
        punished,
        reason,
        automatic
      ))
      .flatMap(punishment -> Mono.when(
        this.sendNotification(guild, punished, punishment)
          .then(this.logToChannel(guild, () -> this.punishments.refresh(punishment))),
        this.applyPunishment(guild, punished, punishment, action)
      ).thenReturn(punishment));
  }

  private @NotNull Mono<?> sendNotification(
    final Guild guild,
    final User user,
    final PunishmentModel.Complete punishment
  ) {
    if (punishment.type().notification()) {
      return user.getPrivateChannel()
        .flatMap(channel -> channel.createMessage(PunishmentMessages.punishmentPunishedDirectMessageEmbed(punishment, guild)))
        .flatMap(message -> this.punishments.update(punishment, new PunishmentModel.Partial.DirectMessageNotified() {
          @Override
          public Snowflake dmNotificationMessageId() {
            return message.getId();
          }
        }))
        // we don't actually care if we can't send a notification to the user
        .onErrorResume(Reactive.ignoringException()); // avoid possible 50007
    }
    return Mono.empty();
  }

  private Mono<?> logToChannel(final Guild guild, final Supplier<Mono<PunishmentModel.Complete>> freshPunishmentSource) {
    return this.guilds.findByGuild(guild.getId())
      .mapNotNull(guildModel -> guildModel.features().punishments().logChannel())
      .flatMap(guild::getChannelById)
      .cast(TextChannel.class)
      .flatMap(channel -> freshPunishmentSource.get().flatMap(punishment -> channel.createMessage(PunishmentDisplay.punishment(punishment, PunishmentDisplayStyle.LOG))));
  }

  private @NotNull Mono<Void> applyPunishment(
    final Guild guild,
    final User user,
    final PunishmentModel.Complete punishment,
    final PunishmentAction<User, PunishmentModel.Complete> action
  ) {
    return ACTUALLY_APPLY_PUNISHMENT
      ? action.apply(guild, user, punishment)
      : Mono.empty();
  }

  public Mono<Void> unenforce(final Guild guild, final PunishmentModel.Complete punishment, final String reason) {
    return switch (punishment.type()) {
      case BAN -> PunishmentAction.unban().apply(guild, punishment.punishedId(), reason)
        .onErrorResume(ClientException.class, Reactive.<Void>ignoringException()); // avoid possible 10026
      case MUTE -> PunishmentAction.unmute().apply(guild, punishment.punishedId(), reason);
      default -> Mono.empty();
    };
  }
}
