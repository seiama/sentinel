package com.seiama.sentinel.feature.punishment;

import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.feature.punishment.display.PunishmentMessages;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.spec.BanQuerySpec;
import discord4j.core.spec.GuildMemberEditSpec;
import discord4j.discordjson.possible.Possible;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.OptionalInt;
import org.jspecify.annotations.NullMarked;
import reactor.core.publisher.Mono;
import reactor.function.Function3;

@NullMarked
public interface PunishmentAction<U, M> extends Function3<Guild, U, M, Mono<Void>> {
  Duration DELETE_MESSAGE_LENGTH = Duration.ofHours(1);

  @Deprecated // You generally don't want to use this.
  static <U, M> PunishmentAction<U, M> noop() {
    return (guild, user, reason) -> Mono.empty();
  }

  static PunishmentAction<User, PunishmentModel.Complete> ban(final Boolean deleteMessages) {
    return ban(
      Boolean.TRUE.equals(deleteMessages)
        ? OptionalInt.of((int) DELETE_MESSAGE_LENGTH.toSeconds())
        : OptionalInt.empty()
    );
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  static PunishmentAction<User, PunishmentModel.Complete> ban(final OptionalInt deleteMessageSeconds) {
    return (guild, user, punishment) -> {
      final BanQuerySpec.Builder spec = BanQuerySpec.builder()
        .reason(PunishmentMessages.punishmentPunishedReason(punishment));
      if (deleteMessageSeconds.isPresent()) {
        spec.deleteMessageSeconds(deleteMessageSeconds.getAsInt());
      }
      return guild.ban(
        user.getId(),
        spec.build()
      );
    };
  }

  @SuppressWarnings("Convert2MethodRef")
  static PunishmentAction<Snowflake, String> unban() {
    return (guild, user, reason) -> guild.unban(
      user,
      reason
    );
  }

  static PunishmentAction<User, PunishmentModel.Complete> kick() {
    return (guild, user, punishment) -> guild.kick(
      user.getId(),
      PunishmentMessages.punishmentPunishedReason(punishment)
    );
  }

  static PunishmentAction<User, PunishmentModel.Complete> mute(final Instant until) {
    return (guild, user, punishment) -> guild.getMemberById(user.getId())
      .flatMap(member -> member.edit(
        GuildMemberEditSpec.builder()
          .communicationDisabledUntilOrNull(until)
          .reason(PunishmentMessages.punishmentPunishedReason(punishment))
          .build()
      )).then();
  }

  static PunishmentAction<Snowflake, String> unmute() {
    return (guild, user, reason) -> guild.getMemberById(user)
      .flatMap(member -> member.edit(
        GuildMemberEditSpec.builder()
          .communicationDisabledUntil(Possible.of(Optional.empty()))
          .reason(reason)
          .build()
      )).then();
  }

  static PunishmentAction<User, PunishmentModel.Complete> note() {
    return (guild, user, punishment) -> Mono.empty();
  }

  static PunishmentAction<User, PunishmentModel.Complete> warn() {
    return (guild, user, punishment) -> Mono.empty();
  }
}
