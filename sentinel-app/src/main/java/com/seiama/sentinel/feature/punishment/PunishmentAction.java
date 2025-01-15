package com.seiama.sentinel.feature.punishment;

import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.feature.punishment.display.PunishmentMessages;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.spec.BanQuerySpec;
import reactor.core.publisher.Mono;
import reactor.function.Function3;

public interface PunishmentAction extends Function3<Guild, User, PunishmentModel.Complete, Mono<Void>> {
  static PunishmentAction ban() {
    return (guild, user, punishment) -> guild.ban(
      user.getId(),
      BanQuerySpec.builder()
        .reason(PunishmentMessages.punishmentPunishedReason(punishment))
        .build()
    );
  }

  static PunishmentAction kick() {
    return (guild, user, punishment) -> guild.kick(
      user.getId(),
      PunishmentMessages.punishmentPunishedReason(punishment)
    );
  }

  static PunishmentAction note() {
    return (guild, user, punishment) -> Mono.empty();
  }

  static PunishmentAction warn() {
    return (guild, user, punishment) -> Mono.empty();
  }
}
