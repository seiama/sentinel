package com.seiama.sentinel.feature.punishment;

import com.seiama.sentinel.common.Listener;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.common.model.PunishmentRepository;
import com.seiama.sentinel.feature.punishment.display.PunishmentMessages;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.spec.BanQuerySpec;
import discord4j.rest.http.client.ClientException;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PunishmentListener implements Listener {
  private final PunishmentRepository punishments;

  @Autowired
  public PunishmentListener(final PunishmentRepository punishments) {
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
              .reason(PunishmentMessages.enforcingExisting(punishment))
              .build()
          ).onErrorResume(ClientException.class, e -> member.kick(PunishmentMessages.enforcingExisting(punishment))));
      })
    );
  }
}
