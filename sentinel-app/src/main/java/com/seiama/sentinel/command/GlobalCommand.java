package com.seiama.sentinel.command;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import reactor.core.publisher.Mono;

@NullMarked
public interface GlobalCommand extends Command {
  Mono<?> on(final GatewayDiscordClient client, final ChatInputInteractionEvent event);

  default Mono<?> suggest(final GatewayDiscordClient client, final ChatInputAutoCompleteEvent event) {
    return event.respondWithSuggestions(List.of());
  }
}
