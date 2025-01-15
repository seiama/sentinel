package com.seiama.sentinel.feature.punishment;

import com.seiama.sentinel.command.Options;
import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.common.model.PunishmentRepository;
import com.seiama.sentinel.feature.punishment.display.PunishmentMessages;
import com.seiama.sentinel.reactive.Reactive;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.discordjson.possible.Possible;
import java.time.Instant;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

public final class PunishmentApplier {
  private static final boolean ACTUALLY_APPLY_PUNISHMENT = true;

  private PunishmentApplier() {
  }

  public static @NotNull Mono<?> apply(
    final ChatInputInteractionEvent event,
    final Guild guild,
    final GuildRepository guilds,
    final PunishmentRepository punishments,
    final PunishmentModel.Type type,
    final PunishmentAction action
  ) {
    final Interaction interaction = event.getInteraction();
    final Member member = interaction.getMember().orElseThrow();
    return event.deferReply().then(
      Options.user(event, Options.MEMBER)
        .orElse(Mono.empty())
        .filterWhen(Punishments.mayPunish(guilds, guild, member))
        .zipWhen(user -> punishments.insert(PunishmentModel.Complete.create(
          guild.getId(),
          type,
          Instant.now(),
          interaction.getUser(),
          user,
          Options.string(event, Options.REASON)
            .orElse(null),
          false
        )))
        .flatMap(TupleUtils.function((user, punishment) -> Mono.whenDelayError(
          sendNotification(guild, user, punishments, punishment),
          applyPunishment(guild, user, punishment, action),
          event.editReply().withContent(Possible.of(Optional.of(PunishmentMessages.punishmentPunisherResponse(punishment))))
        )))
    );
  }

  private static @NotNull Mono<?> sendNotification(
    final Guild guild,
    final User user,
    final PunishmentRepository punishments,
    final PunishmentModel.Complete punishment
  ) {
    if (punishment.type().notification()) {
      return user.getPrivateChannel()
        .flatMap(channel -> channel.createMessage(PunishmentMessages.punishmentPunishedDirectMessageEmbed(punishment, guild)))
        .flatMap(message -> punishments.update(punishment, new PunishmentModel.Partial.DirectMessageNotified() {
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

  private static @NotNull Mono<?> applyPunishment(
    final Guild guild,
    final User user,
    final PunishmentModel.Complete punishment,
    final PunishmentAction action
  ) {
    return ACTUALLY_APPLY_PUNISHMENT
      ? action.apply(guild, user, punishment)
      : Mono.empty();
  }
}
