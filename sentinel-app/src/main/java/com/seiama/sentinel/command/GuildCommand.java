package com.seiama.sentinel.command;

import com.seiama.sentinel.common.model.Feature;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Guild;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface GuildCommand extends Command {
  @NotNull Feature feature();

  @NotNull Mono<?> on(final @NotNull GatewayDiscordClient client, final @NotNull ChatInputInteractionEvent event, final @NotNull Guild guild);
}
