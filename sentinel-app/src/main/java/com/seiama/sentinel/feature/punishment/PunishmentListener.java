package com.seiama.sentinel.feature.punishment;

import com.seiama.sentinel.common.Listener;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.feature.punishment.display.PunishmentMessages;
import com.seiama.sentinel.reactive.Reactive;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.AuditLogEntryCreateEvent;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.audit.ActionType;
import discord4j.core.object.audit.AuditLogChange;
import discord4j.core.object.audit.AuditLogEntry;
import discord4j.core.object.audit.ChangeKey;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.spec.BanQuerySpec;
import discord4j.rest.http.client.ClientException;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

@Component
public class PunishmentListener implements Listener {
  private static final Set<ActionType> AUDIT_LOG_EVENTS = Set.of(
    ActionType.MEMBER_KICK,
    ActionType.MEMBER_BAN_ADD,
    ActionType.MEMBER_BAN_REMOVE,
    ActionType.MEMBER_UPDATE
  );
  private final Punishments punishments;

  @Autowired
  public PunishmentListener(final Punishments punishments) {
    this.punishments = punishments;
  }

  @Override
  public @NotNull Mono<Void> listen(final @NotNull GatewayDiscordClient client) {
    return Mono.when(
      client.on(MemberJoinEvent.class, event -> {
        final Member member = event.getMember();
        return this.punishments.findActive(event.getGuildId(), member.getId(), PunishmentModel.Type.BAN)
          .next()
          .flatMap(punishment -> member.ban(
            BanQuerySpec.builder()
              .reason(PunishmentMessages.enforcingExisting(punishment))
              .build()
          ).onErrorResume(ClientException.class, e -> member.kick(PunishmentMessages.enforcingExisting(punishment))));
      }),
      client.on(AuditLogEntryCreateEvent.class, event -> {
        final AuditLogEntry entry = event.getAuditLogEntry();
        if (AUDIT_LOG_EVENTS.contains(entry.getActionType())) {
          final Snowflake responsibleUserId = entry.getResponsibleUserId().orElse(null);
          if (client.getSelfId().equals(responsibleUserId)) {
            return Mono.empty();
          }
          return Reactive.zipSequence(
            event.getGuild(),
            entry.getTargetId()
              .map(client::getUserById)
              .orElse(Mono.empty()),
            this.tryGetUser(client, entry)
          ).flatMap(TupleUtils.function((guild, punished, punisher) -> {
            final String reason = entry.getReason().orElse(null);
            final boolean automatic = false;
            final ActionType action = entry.getActionType();
            return switch (action) {
              case MEMBER_KICK, MEMBER_BAN_ADD, MEMBER_UPDATE -> {
                @Nullable AuditLogChange<Instant> communicationDisabledUntil = null;
                final PunishmentModel.Type type = switch (action) {
                  case MEMBER_KICK -> PunishmentModel.Type.KICK;
                  case MEMBER_BAN_ADD -> PunishmentModel.Type.BAN;
                  case MEMBER_UPDATE -> {
                    communicationDisabledUntil = entry.getChange(ChangeKey.COMMUNICATION_DISABLED_UNTIL).orElse(null);
                    if (communicationDisabledUntil != null && communicationDisabledUntil.getCurrentValue().isPresent()) {
                      yield PunishmentModel.Type.MUTE;
                    } else {
                      yield null; // unmute, handled below
                    }
                  }
                  default -> null;
                };
                if (type != null) {
                  yield this.punishments.create(
                    guild,
                    PunishmentModel.Complete.create(
                      guild.getId(),
                      type,
                      Instant.now(),
                      punisher,
                      punished,
                      reason,
                      communicationDisabledUntil != null
                        ? this.resolveDurationFrom(communicationDisabledUntil)
                        : null,
                      automatic
                    ),
                    punished,
                    PunishmentAction.noop()
                  );
                } else if (communicationDisabledUntil != null) {
                  yield this.punishments.findActive(guild.getId(), punished.getId(), PunishmentModel.Type.MUTE)
                    .next()
                    .flatMap(model -> {
                      return this.punishments.repository().update(model, PunishmentModel.Partial.Stale.of(
                        punisher,
                        reason,
                        automatic,
                        null
                      ));
                    });
                } else {
                  yield Mono.empty();
                }
              }
              case MEMBER_BAN_REMOVE -> {
                yield this.punishments.findActive(guild.getId(), punished.getId(), PunishmentModel.Type.BAN)
                  .next()
                  .flatMap(model -> {
                    return this.punishments.repository().update(model, PunishmentModel.Partial.Stale.of(
                      punisher,
                      reason,
                      automatic,
                      null
                    ));
                  });
              }
              default -> Mono.empty();
            };
          }));
        }
        return Mono.empty();
      })
    );
  }

  private Mono<Optional<User>> tryGetUser(final GatewayDiscordClient client, final AuditLogEntry entry) {
    final Optional<Snowflake> id = entry.getResponsibleUserId();
    if (id.isPresent()) {
      if (entry.getParent() != null) {
        final Optional<User> user = entry.getResponsibleUser();
        if (user.isPresent()) {
          return Mono.just(user);
        }
      }
      return id
        .map(client::getUserById)
        .orElseThrow()
        .map(Optional::of);
    }
    return Mono.just(Optional.empty());
  }

  private @Nullable Duration resolveDurationFrom(final AuditLogChange<Instant> change) {
    if (change != null) {
      final Optional<Instant> value = change.getCurrentValue();
      if (value.isPresent()) {
        return Duration.between(Instant.now(), value.get());
      }
    }
    return null;
  }
}
