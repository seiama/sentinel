package com.seiama.sentinel.feature.punishment.creator;

import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.feature.punishment.PunishmentAction;
import com.seiama.sentinel.feature.punishment.Punishments;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import java.time.Duration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

@NullMarked
public final class UserInteractionPunishmentCreator extends AbstractInteractionPunishmentCreator<DeferrableInteractionEvent> implements Punishments.Creator {
  private final User user;
  private final @Nullable String reason;

  public UserInteractionPunishmentCreator(
    final GatewayDiscordClient client,
    final DeferrableInteractionEvent event,
    final Guild guild,
    final PunishmentModel.Type type,
    final @Nullable Duration duration,
    final PunishmentAction<User, PunishmentModel.Complete> action,
    final User user,
    final @Nullable String reason
  ) {
    super(client, event, guild, type, duration, action);
    this.user = user;
    this.reason = reason;
  }

  @Override
  protected Mono<User> getPunishedUser() {
    return Mono.justOrEmpty(this.user);
  }

  @Override
  protected @Nullable String getReason() {
    return this.reason;
  }
}
