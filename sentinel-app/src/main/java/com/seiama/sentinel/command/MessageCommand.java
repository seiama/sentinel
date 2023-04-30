package com.seiama.sentinel.command;

import com.seiama.sentinel.common.model.Feature;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import discord4j.core.object.entity.Guild;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface MessageCommand extends Command {
  @NotNull Feature feature();

  @NotNull Mono<?> on(final @NotNull GatewayDiscordClient client, final @NotNull MessageInteractionEvent event, final @NotNull Guild guild);
}
