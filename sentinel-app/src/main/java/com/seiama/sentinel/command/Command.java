package com.seiama.sentinel.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.discordjson.json.ApplicationCommandRequest;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface Command {
  @NotNull String name();

  @NotNull ApplicationCommandRequest request();

  static @NotNull Mono<?> executeOne(final @NotNull ChatInputInteractionEvent event, final @NotNull Map<String, Executable> executables) {
    return Mono.defer(() -> {
      return executables.entrySet()
        .stream()
        .map(entry -> event.getOption(entry.getKey()).map(option -> entry.getValue().execute(option)).orElse(null))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(Mono.empty());
    });
  }

  @FunctionalInterface
  interface Executable {
    @NotNull Mono<?> execute(final @NotNull ApplicationCommandInteractionOption option);
  }
}
