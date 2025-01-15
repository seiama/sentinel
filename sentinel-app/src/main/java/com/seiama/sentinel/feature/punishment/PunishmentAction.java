package com.seiama.sentinel.feature.punishment;

import com.seiama.sentinel.command.Options;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.common.model.PunishmentRepository;
import com.seiama.sentinel.feature.punishment.display.PunishmentMessages;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.spec.BanQuerySpec;
import discord4j.discordjson.possible.Possible;
import java.time.Instant;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;
import reactor.function.Function3;
import reactor.function.TupleUtils;

public final class PunishmentAction {
  public static final Function3<Guild, User, PunishmentModel.Complete, Mono<Void>> BAN = (guild, user, punishment) -> guild.ban(
    user.getId(),
    BanQuerySpec.builder()
      .reason(PunishmentMessages.punishmentPunishedReason(punishment))
      .build()
  );
  public static final Function3<Guild, User, PunishmentModel.Complete, Mono<Void>> KICK = (guild, user, punishment) -> guild.kick(
    user.getId(),
    PunishmentMessages.punishmentPunishedReason(punishment)
  );
  public static final Function3<Guild, User, PunishmentModel.Complete, Mono<Void>> WARN = (guild, user, punishment) -> Mono.empty();
  private static final boolean ACTUALLY_APPLY_PUNISHMENT = true;

  private PunishmentAction() {
  }

  private static @NotNull Mono<?> notifyAndApply(
    final Guild guild,
    final User user,
    final PunishmentRepository punishments,
    final PunishmentModel.Complete punishment,
    final Function3<Guild, User, PunishmentModel.Complete, Mono<Void>> action
  ) {
    return Mono.whenDelayError(
      user.getPrivateChannel()
        .flatMap(channel -> channel.createMessage(PunishmentMessages.punishmentPunishedDirectMessageEmbed(punishment, guild)))
        .flatMap(message -> punishments.update(punishment, new PunishmentModel.Partial.DirectMessageNotified() {
          @Override
          public Snowflake dmNotificationMessageId() {
            return message.getId();
          }
        }))
        // we don't actually care if we can't send a notification to the user
        .onErrorResume(t -> Mono.empty()), // avoid possible 50007
      ACTUALLY_APPLY_PUNISHMENT ? action.apply(guild, user, punishment) : Mono.empty()
    );
  }

  public static @NotNull Mono<?> apply(
    final ChatInputInteractionEvent event,
    final Guild guild,
    final PunishmentRepository punishments,
    final PunishmentModel.Type type,
    final Function3<Guild, User, PunishmentModel.Complete, Mono<Void>> action
  ) {
    final Interaction interaction = event.getInteraction();
    return event.deferReply().then(Options.user(event, Options.MEMBER)
      .orElse(Mono.empty())
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
        notifyAndApply(guild, user, punishments, punishment, action),
        event.editReply().withContent(Possible.of(Optional.of(PunishmentMessages.punishmentPunisherResponse(punishment))))
      ))));
  }
}
