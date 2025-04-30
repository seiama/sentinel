package com.seiama.sentinel.command;

import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.User;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import reactor.core.publisher.Mono;

@NullMarked
public final class Options {
  private Options() {
  }

  public static Optional<Mono<User>> user(final ApplicationCommandInteractionOption option, final String name) {
    return option.getOption(name)
      .flatMap(ApplicationCommandInteractionOption::getValue)
      .map(ApplicationCommandInteractionOptionValue::asUser);
  }

  public static Optional<String> string(final ApplicationCommandInteractionOption option, final String name) {
    return option.getOption(name)
      .flatMap(ApplicationCommandInteractionOption::getValue)
      .map(ApplicationCommandInteractionOptionValue::asString);
  }
}
