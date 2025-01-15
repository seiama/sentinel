package com.seiama.sentinel.command;

import com.seiama.sentinel.common.model.Feature;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import discord4j.core.object.entity.Guild;
import org.jspecify.annotations.NullMarked;
import reactor.core.publisher.Mono;

@NullMarked
public interface MessageCommand extends Command {
  Feature feature();

  Mono<?> on(final GatewayDiscordClient client, final MessageInteractionEvent event, final Guild guild);
}
