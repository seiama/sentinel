package com.seiama.sentinel.feature.punishment;

import com.seiama.sentinel.common.discord.KnownBots;
import com.seiama.sentinel.common.discord.UserDisplay;
import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.common.model.PunishmentRepository;
import com.seiama.sentinel.common.model.UserIdentity;
import com.seiama.sentinel.feature.punishment.display.PunishmentDisplay;
import com.seiama.sentinel.feature.punishment.display.PunishmentDisplayStyle;
import com.seiama.sentinel.feature.punishment.display.PunishmentMessages;
import com.seiama.sentinel.reactive.Reactive;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.StartThreadWithoutMessageRequest;
import discord4j.discordjson.json.ThreadModifyRequest;
import discord4j.rest.RestClient;
import discord4j.rest.entity.RestChannel;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.AllowedMentions;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@NullMarked
public final class Punishments {
  private static final Set<Snowflake> SHOULD_BE_ASSUMED_AS_AUTOMATIC_BY = Set.of(
    KnownBots.BEEMO_ID
  );
  private static final boolean ACTUALLY_APPLY_PUNISHMENT = true;
  private static final boolean ACTUALLY_NOTIFY_USER = true;
  private static final int THREAD_AUTO_ARCHIVE_DURATION = 10080; // 7 days, in minutes
  private final GuildRepository guilds;
  private final PunishmentRepository punishments;

  @Autowired
  public Punishments(
    final GuildRepository guilds,
    final PunishmentRepository punishments
  ) {
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

  public Mono<PunishmentModel.Complete> create(
    final GatewayDiscordClient client,
    final Guild guild,
    final PunishmentModel.Complete punishment,
    final User punished,
    final PunishmentAction<User, PunishmentModel.Complete> action
  ) {
    return this.punishments
      .insert(punishment)
      .flatMap(model -> Mono.when(
        this.sendNotification(client, guild, punished, model)
          .then(this.logToChannel(guild, () -> this.punishments.refresh(model))),
        this.applyPunishment(guild, punished, model, action)
      ).thenReturn(model));
  }

  private Mono<?> sendNotification(
    final GatewayDiscordClient client,
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
        .onErrorResume(t -> this.createPrivateThreadNotification(client, guild, user, punishment)); // avoid possible 50007
    }
    return Mono.empty();
  }

  private Mono<PunishmentModel.Complete> createPrivateThreadNotification(
    final GatewayDiscordClient client,
    final Guild guild,
    final User user,
    final PunishmentModel.Complete punishment
  ) {
    if (punishment.type().notification() && !punishment.type().terminal() && ACTUALLY_NOTIFY_USER) {
      final RestClient rest = client.rest();
      return this.guilds.findByGuild(guild.getId())
        .mapNotNull(guildModel -> guildModel.features().punishments().privateThreadNotificationChannel())
        .flatMap(id -> rest.getChannelService().startThreadWithoutMessage(
          id.asLong(),
          StartThreadWithoutMessageRequest.builder()
            .type(Channel.Type.GUILD_PRIVATE_THREAD.getValue())
            .name(UserDisplay.render(UserDisplay.Renderer.USERNAME, new UserIdentity(user)))
            .autoArchiveDuration(THREAD_AUTO_ARCHIVE_DURATION)
            .build()
        ))
        .flatMap(threadData -> {
          final RestChannel thread = rest.getChannelById(Snowflake.of(threadData.id()));
          return thread.createMessage(PunishmentMessages.punishmentPunishedDirectMessageEmbed(punishment, guild).asRequest())
            .flatMap(message -> Mono.when(
              rest.getChannelService().addThreadMember(thread.getId().asLong(), user.getId().asLong()),
              rest.getChannelService().modifyThread(thread.getId().asLong(), ThreadModifyRequest.builder().locked(true).build(), null)
            ).thenReturn(thread));
        })
        .flatMap(thread -> this.punishments.update(punishment, new PunishmentModel.Partial.PrivateThreadNotified() {
          @Override
          public Snowflake privateNotificationThreadId() {
            return thread.getId();
          }
        }));
    }
    return Mono.empty();
  }

  private Mono<?> logToChannel(final Guild guild, final Supplier<Mono<PunishmentModel.Complete>> freshPunishmentSource) {
    return this.guilds.findByGuild(guild.getId())
      .mapNotNull(guildModel -> guildModel.features().punishments().logChannel())
      .flatMap(guild::getChannelById)
      .cast(TextChannel.class)
      .flatMap(channel -> freshPunishmentSource.get().flatMap(punishment -> channel.createMessage()
        .withAllowedMentions(AllowedMentions.suppressAll())
        .withFlags(Message.Flag.IS_COMPONENTS_V2)
        .withComponents(PunishmentDisplay.punishment(punishment, PunishmentDisplayStyle.LOG))
      ));
  }

  private Mono<Void> applyPunishment(
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

  static boolean shouldBeAssumedAsAutomatic(final Optional<User> punisher) {
    return punisher
      .map(user -> user.isBot() || SHOULD_BE_ASSUMED_AS_AUTOMATIC_BY.contains(user.getId()))
      .orElse(false);
  }

  @FunctionalInterface
  public interface Creator {
    Mono<PunishmentModel.Complete> create(final GuildRepository guilds, final Punishments punishments);
  }
}
