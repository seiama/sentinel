package com.seiama.sentinel.feature.punishment.creator;

import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.feature.punishment.PunishmentAction;
import com.seiama.sentinel.feature.punishment.Punishments;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import java.time.Duration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

@NullMarked
public final class MessageInteractionPunishmentCreator extends AbstractInteractionPunishmentCreator<DeferrableInteractionEvent> implements Punishments.Creator {
  private final Message message;
  private final @Nullable String reason;

  public MessageInteractionPunishmentCreator(
    final GatewayDiscordClient client,
    final DeferrableInteractionEvent event,
    final Guild guild,
    final PunishmentModel.Type type,
    final @Nullable Duration duration,
    final PunishmentAction<User, PunishmentModel.Complete> action,
    final Message message,
    final @Nullable String reason
  ) {
    super(client, event, guild, type, duration, action);
    this.message = message;
    this.reason = reason;
  }

  @Override
  protected Mono<User> getPunishedUser() {
    return Mono.justOrEmpty(this.message.getAuthor());
  }

  @Override
  protected @Nullable String getReason() {
    return this.reason;
  }
}
