package com.seiama.sentinel.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
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

  public static Optional<Mono<User>> user(final ChatInputInteractionEvent event, final String name) {
    return user(event.getOption(name));
  }

  public static Optional<Mono<User>> user(final ApplicationCommandInteractionOption option, final String name) {
    return user(option.getOption(name));
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private static Optional<Mono<User>> user(final Optional<ApplicationCommandInteractionOption> option) {
    return option
      .flatMap(ApplicationCommandInteractionOption::getValue)
      .map(ApplicationCommandInteractionOptionValue::asUser);
  }

  public static Optional<Boolean> bool(final ChatInputInteractionEvent event, final String name) {
    return bool(event.getOption(name));
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private static Optional<Boolean> bool(final Optional<ApplicationCommandInteractionOption> option) {
    return option
      .flatMap(ApplicationCommandInteractionOption::getValue)
      .map(ApplicationCommandInteractionOptionValue::asBoolean);
  }

  public static Optional<String> string(final ChatInputInteractionEvent event, final String name) {
    return string(event.getOption(name));
  }

  public static Optional<String> string(final ApplicationCommandInteractionOption option, final String name) {
    return string(option.getOption(name));
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public static Optional<String> string(final Optional<ApplicationCommandInteractionOption> option) {
    return option
      .flatMap(ApplicationCommandInteractionOption::getValue)
      .map(ApplicationCommandInteractionOptionValue::asString);
  }
}
