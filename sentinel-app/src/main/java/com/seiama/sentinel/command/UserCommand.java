package com.seiama.sentinel.command;

import com.seiama.sentinel.common.model.Feature;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.UserInteractionEvent;
import discord4j.core.object.entity.Guild;
import org.jspecify.annotations.NullMarked;
import reactor.core.publisher.Mono;

@NullMarked
public interface UserCommand extends Command {
  Feature feature();

  Mono<?> on(final GatewayDiscordClient client, final UserInteractionEvent event, final Guild guild);
}
