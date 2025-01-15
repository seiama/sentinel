package com.seiama.sentinel.command;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.User;
import java.util.Optional;
import reactor.core.publisher.Mono;

public final class Options {
  public static final String CONTENT = "content";
  public static final String DELETE_MESSAGES = "delete_messages";
  public static final String DESCRIPTION = "description";
  public static final String MEMBER = "member";
  public static final String MESSAGE = "message";
  public static final String NAME = "name";
  public static final String PUNISHMENT = "punishment";
  public static final String REASON = "reason";
  public static final String USER = "user";

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
