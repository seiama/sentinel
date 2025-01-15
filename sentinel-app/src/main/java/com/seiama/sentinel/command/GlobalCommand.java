package com.seiama.sentinel.command;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface GlobalCommand extends Command {
  @NotNull Mono<?> on(final @NotNull GatewayDiscordClient client, final @NotNull ChatInputInteractionEvent event);
}
