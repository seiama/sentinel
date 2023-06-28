package com.seiama.sentinel.feature.punishment;

import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.common.model.PunishmentRepository;
import com.seiama.sentinel.common.model.UserIdentity;
import com.seiama.sentinel.feature.punishment.display.PunishmentDisplay;
import com.seiama.sentinel.feature.punishment.display.PunishmentDisplayStyle;
import com.seiama.sentinel.feature.punishment.display.PunishmentMessages;
import com.seiama.sentinel.reactive.Reactive;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.http.client.ClientException;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public final class Punishments {
  private static final Set<Snowflake> SHOULD_BE_ASSUMED_AS_AUTOMATIC_BY = Set.of(
    UserIdentity.BEEMO_ID
  );
  private static final boolean ACTUALLY_APPLY_PUNISHMENT = true;
  private static final boolean ACTUALLY_NOTIFY_USER = true;
  private final GuildRepository guilds;
  private final PunishmentRepository punishments;

  @Autowired
  private Punishments(final GuildRepository guilds, final PunishmentRepository punishments) {
    this.guilds = guilds;
    this.punishments = punishments;
  }

  public PunishmentRepository repository() {
    return this.punishments;
  }

  public Flux<PunishmentModel.Complete> findActive(final Snowflake guild, final Snowflake punishedId, final PunishmentModel.Type type) {
    return this.punishments.findAllByGuildAndPunishedIdAndTypeAndStaleIsNotOrderByDateDesc(guild, punishedId, type, true);
  }

  public Mono<PunishmentModel.Complete> createUsing(final Creator creator) {
    return creator.create(this.guilds, this);
  }

  public @NotNull Mono<PunishmentModel.Complete> create(
    final Guild guild,
    final PunishmentModel.Complete punishment,
    final User punished,
    final PunishmentAction<User, PunishmentModel.Complete> action
  ) {
    return this.punishments
      .insert(punishment)
      .flatMap(model -> Mono.when(
        this.sendNotification(guild, punished, model)
          .then(this.logToChannel(guild, () -> this.punishments.refresh(model))),
        this.applyPunishment(guild, punished, model, action)
      ).thenReturn(model));
  }

  private @NotNull Mono<?> sendNotification(
    final Guild guild,
    final User user,
    final PunishmentModel.Complete punishment
  ) {
    if (punishment.type().notification() && ACTUALLY_NOTIFY_USER) {
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

  boolean shouldBeAssumedAsAutomatic(final Optional<User> punisher) {
    return punisher
      .map(User::getId)
      .map(SHOULD_BE_ASSUMED_AS_AUTOMATIC_BY::contains)
      .orElse(false);
  }

  @FunctionalInterface
  public interface Creator {
    Mono<PunishmentModel.Complete> create(final GuildRepository guilds, final Punishments punishments);
  }
}
