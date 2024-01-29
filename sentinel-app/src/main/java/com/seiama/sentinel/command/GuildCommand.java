package com.seiama.sentinel.command;

import com.seiama.sentinel.common.model.Feature;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Guild;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import reactor.core.publisher.Mono;

@NullMarked
public interface GuildCommand extends Command {
  Feature feature();

  Mono<?> on(final GatewayDiscordClient client, final ChatInputInteractionEvent event, final Guild guild);

  default Mono<?> suggest(final GatewayDiscordClient client, final ChatInputAutoCompleteEvent event, final Guild guild) {
    return event.respondWithSuggestions(List.of());
  }
}
