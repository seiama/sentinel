package com.seiama.sentinel.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.discordjson.json.ApplicationCommandRequest;
import java.util.Map;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import reactor.core.publisher.Mono;

@NullMarked
public interface Command {
  String name();

  ApplicationCommandRequest request();

  static Mono<?> executeOne(final ChatInputInteractionEvent event, final Map<String, Executable> executables) {
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
    Mono<?> execute(final ApplicationCommandInteractionOption option);
  }
}
