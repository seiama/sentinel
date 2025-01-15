package com.seiama.sentinel.command;

import com.seiama.sentinel.feature.Feature;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Guild;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface GuildCommand extends Command {
  @NotNull Feature feature();

  default boolean test(final ChatInputInteractionEvent event, final Guild guild) {
    return Command.super.test(event);
  }

  @NotNull Mono<?> on(final @NotNull ChatInputInteractionEvent event, final @NotNull Guild guild);
}
