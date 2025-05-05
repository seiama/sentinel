package com.seiama.sentinel.feature.punishment.creator;

import com.seiama.sentinel.command.OptionNames;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.feature.punishment.PunishmentAction;
import com.seiama.sentinel.feature.punishment.Punishments;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import java.time.Duration;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

@NullMarked
public final class ChatInteractionPunishmentCreator extends AbstractInteractionPunishmentCreator<ChatInputInteractionEvent> implements Punishments.Creator {
  public ChatInteractionPunishmentCreator(
    final GatewayDiscordClient client,
    final ChatInputInteractionEvent event,
    final Guild guild,
    final PunishmentModel.Type type,
    final @Nullable Duration duration,
    final PunishmentAction<User, PunishmentModel.Complete> action
  ) {
    super(client, event, guild, type, duration, action);
  }

  @Override
  protected Mono<User> getPunishedUser() {
    return this.event.getOptionAsUser(OptionNames.MEMBER);
  }

  @Override
  protected @Nullable String getReason() {
    return this.event.getOptionAsString(OptionNames.REASON).orElse(null);
  }
}
