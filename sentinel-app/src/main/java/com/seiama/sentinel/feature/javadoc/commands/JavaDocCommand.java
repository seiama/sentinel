package com.seiama.sentinel.feature.javadoc.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seiama.sentinel.command.Command;
import com.seiama.sentinel.command.GuildCommand;
import com.seiama.sentinel.command.OptionNames;
import com.seiama.sentinel.command.Options;
import com.seiama.sentinel.common.discord.Emoji;
import com.seiama.sentinel.common.model.FactoidModel;
import com.seiama.sentinel.common.model.Feature;
import com.seiama.sentinel.common.model.JavaDocModel;
import com.seiama.sentinel.common.model.JavaDocRepository;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@NullMarked
public final class JavaDocCommand implements GuildCommand {

  private static final String NAME = "javadocs";

  private static final String SET = "set";
  private static final String REMOVE = "remove";

  private final JavaDocRepository javadocs;
  private final RestClient rest;
  private final org.springframework.web.client.RestClient http;
  private final ObjectMapper mapper;

  @Autowired
  private JavaDocCommand(final JavaDocRepository javadocs, final @Qualifier("javadocsRest") RestClient rest, final org.springframework.web.client.RestClient.Builder http, final ObjectMapper mapper) {
    this.javadocs = javadocs;
    this.rest = rest;
    this.http = http.build();
    this.mapper = mapper;
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public ApplicationCommandRequest request() {
    return ApplicationCommandRequest.builder()
      .name(NAME)
      .description("Manage javadocs")
      .defaultPermission(false)
      .addOption(
        ApplicationCommandOptionData.builder()
          .name(SET)
          .description("Set details about a javadoc")
          .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
          .addOption(
            ApplicationCommandOptionData.builder()
              .name(OptionNames.NAME)
              .description("The name of the javadoc to set")
              .required(true)
              .type(ApplicationCommandOption.Type.STRING.getValue())
              .autocomplete(true)
              .build()
          )
          .addOption(
            ApplicationCommandOptionData.builder()
              .name(OptionNames.JAVADOC)
              .description("The url of the javadoc")
              .required(true)
              .type(ApplicationCommandOption.Type.STRING.getValue())
              .build()
          )
          .build()
      )
      .addOption(
        ApplicationCommandOptionData.builder()
          .name(REMOVE)
          .description("Remove a javadoc")
          .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
          .addOption(
            ApplicationCommandOptionData.builder()
              .name(OptionNames.NAME)
              .description("The name of the javadoc to remove")
              .required(true)
              .type(ApplicationCommandOption.Type.STRING.getValue())
              .autocomplete(true)
              .build()
          )
          .build()
      )
      .build();
  }

  @Override
  public Feature feature() {
    return Feature.JAVADOCS;
  }

  @Override
  public Mono<?> on(final GatewayDiscordClient client, final ChatInputInteractionEvent event, final Guild guild) {
    return event.deferReply()
      .withEphemeral(true)
      .then(Command.executeOne(event, Map.of(
        SET, option -> {
          return Mono.justOrEmpty(Options.string(option, OptionNames.NAME))
            .flatMap(name -> {
              final String url = Options.string(option, OptionNames.JAVADOC).orElseThrow(); // This is mandatory in all cases
              // TODO: need parse that url to check if is a javadoc in fist place
              return this.javadocs.findByGuildAndName(guild.getId(), name)
                .flatMap(model -> this.javadocs.update(model, new JavaDocModel.Partial.SetUrl() {
                  @Override
                  public String url() {
                    return url;
                  }
                }))
                // TODO: the user javadoc can be /javadoc-{name} and need register that
                .switchIfEmpty(this.javadocs.insert(new JavaDocModel.Complete(new ObjectId(), guild.getId(), name, url, null)))
                .flatMap(model -> {
                  if (model.commandId() == null) {
                    return this.appAction((applicationId, service) -> service.createGuildApplicationCommand(
                      applicationId,
                      guild.getId().asLong(),
                      model.asRequest()
                    )).flatMap(data -> this.javadocs.update(model, new JavaDocModel.Partial.SetCommandId() {
                      @Override
                      public Snowflake commandId() {
                        return Snowflake.of(data.id());
                      }
                    }));
                  }
                  return Mono.empty();
                })
                .then(event.editReply().withContentOrNull(Emoji.YES.asFormat()));
            });
        },
        REMOVE, option -> {
          return Mono.justOrEmpty(Options.string(option, OptionNames.NAME))
            .flatMap(name -> {
              return this.javadocs.findByGuildAndName(guild.getId(), name)
                .switchIfEmpty(event.editReply().withContentOrNull("%s Could not find a javadoc with name `%s`.".formatted(Emoji.NO.asFormat(), name)).then(Mono.empty()))
                .flatMap(model -> this.appAction((id, service) -> service.deleteGuildApplicationCommand(id, guild.getId().asLong(), model.commandId().asLong())).thenReturn(model))
                .flatMap(this.javadocs::delete)
                .then(event.editReply().withContentOrNull(Emoji.YES.asFormat()));
            });
        }
      )));
  }

  @Override
  public Mono<?> suggest(final GatewayDiscordClient client, final ChatInputAutoCompleteEvent event, final Guild guild) {
    final ApplicationCommandInteractionOption option = event.getFocusedOption();
    final Optional<ApplicationCommandInteractionOptionValue> value = option.getValue();
    if (value.isPresent()) {
      if (OptionNames.NAME.equals(option.getName())) {
        return this.javadocs.findAllByGuild(guild.getId())
          .filter(model -> model.name().startsWith(value.orElseThrow().asString()))
          .map(model -> {
            return ApplicationCommandOptionChoiceData.builder()
              .name(model.name())
              .value(model.name())
              .build();
          })
          .cast(ApplicationCommandOptionChoiceData.class)
          .collectList()
          .flatMap(event::respondWithSuggestions);
      }
    }
    return GuildCommand.super.suggest(client, event, guild);
  }

  private <T> Mono<T> appAction(final BiFunction<Long, ApplicationService, Mono<T>> consumer) {
    return this.rest.getApplicationId().flatMap(applicationId -> consumer.apply(applicationId, this.rest.getApplicationService()));
  }
}
