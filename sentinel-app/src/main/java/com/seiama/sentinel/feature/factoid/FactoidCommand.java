package com.seiama.sentinel.feature.factoid;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seiama.sentinel.command.Command;
import com.seiama.sentinel.command.GuildCommand;
import com.seiama.sentinel.command.OptionNames;
import com.seiama.sentinel.command.Options;
import com.seiama.sentinel.common.discord.Emoji;
import com.seiama.sentinel.common.model.FactoidModel;
import com.seiama.sentinel.common.model.FactoidRepository;
import com.seiama.sentinel.common.model.Feature;
import com.seiama.sentinel.common.model.response.Response;
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
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@NullMarked
public final class FactoidCommand implements GuildCommand {
  private static final String NAME = "factoid";

  private static final String SET = "set";
  private static final String REMOVE = "remove";

  private final FactoidRepository factoids;
  private final RestClient rest;
  private final org.springframework.web.client.RestClient http;
  private final ObjectMapper mapper;

  @Autowired
  private FactoidCommand(final FactoidRepository factoids, final @Qualifier("factoidsRest") RestClient rest, final org.springframework.web.client.RestClient.Builder http, final ObjectMapper mapper) {
    this.factoids = factoids;
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
      .description("Manage factoids")
      .defaultPermission(false)
      .addOption(
        ApplicationCommandOptionData.builder()
          .name(SET)
          .description("Set details about a factoid")
          .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
          .addOption(
            ApplicationCommandOptionData.builder()
              .name(OptionNames.NAME)
              .description("The name of the factoid to set")
              .required(true)
              .type(ApplicationCommandOption.Type.STRING.getValue())
              .autocomplete(true)
              .build()
          )
          .addOption(
            ApplicationCommandOptionData.builder()
              .name(OptionNames.DESCRIPTION)
              .description("The description for the factoid")
              .required(false)
              .type(ApplicationCommandOption.Type.STRING.getValue())
              .build()
          )
          .addOption(
            ApplicationCommandOptionData.builder()
              .name(OptionNames.CONTENT)
              .description("The content for the factoid")
              .required(false)
              .type(ApplicationCommandOption.Type.STRING.getValue())
              .build()
          )
          .addOption(
            ApplicationCommandOptionData.builder()
              .name(OptionNames.JSON)
              .description("The URL to JSON for the factoid.")
              .required(false)
              .type(ApplicationCommandOption.Type.STRING.getValue())
              .build()
          )
          .build()
      )
      .addOption(
        ApplicationCommandOptionData.builder()
          .name(REMOVE)
          .description("Remove a factoid")
          .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
          .addOption(
            ApplicationCommandOptionData.builder()
              .name(OptionNames.NAME)
              .description("The name of the factoid to remove")
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
    return Feature.FACTOIDS;
  }

  @Override
  public Mono<?> on(final GatewayDiscordClient client, final ChatInputInteractionEvent event, final Guild guild) {
    return event.deferReply()
      .withEphemeral(true)
      .then(Command.executeOne(event, Map.of(
        SET, option -> {
          return Mono.justOrEmpty(Options.string(option, OptionNames.NAME))
            .flatMap(name -> {
              final Optional<String> description = Options.string(option, OptionNames.DESCRIPTION);
              final Optional<String> content = Options.string(option, OptionNames.CONTENT);
              final Optional<String> json = Options.string(option, OptionNames.JSON);
              final Response response;
              if (json.isPresent()) {
                try {
                  response = this.mapper.readValue(
                    this.http.get()
                      .uri(URI.create(json.get()))
                      .retrieve()
                      .toEntity(String.class)
                      .getBody(),
                    Response.class
                  );
                } catch (final Throwable t) {
                  t.printStackTrace();
                  return event.editReply().withContentOrNull("Something went wrong.");
                }
              } else if (content.isPresent()) {
                response = new Response(
                  content.orElse(null),
                  List.of(),
                  List.of()
                );
              } else {
                response = null;
              }
              return this.factoids.findByGuildAndName(guild.getId(), name)
                .flatMap(model -> this.factoids.update(model, new FactoidModel.Partial.SetDescriptionAndResponse() {
                  @Override
                  public @Nullable String description() {
                    return description.orElse(null);
                  }

                  @Override
                  public @Nullable Response response() {
                    return response;
                  }
                }))
                .switchIfEmpty(this.factoids.insert(new FactoidModel.Complete(new ObjectId(), guild.getId(), name, description.orElse("(description not set)"), response, null)))
                .flatMap(model -> {
                  if (model.commandId() == null) {
                    return this.appAction((applicationId, service) -> service.createGuildApplicationCommand(
                      applicationId,
                      guild.getId().asLong(),
                      model.asRequest()
                    )).flatMap(data -> this.factoids.update(model, new FactoidModel.Partial.SetCommandId() {
                      @Override
                      public Snowflake commandId() {
                        return Snowflake.of(data.id());
                      }
                    }));
                  } else {
                    if (description.isPresent()) {
                      return this.appAction((applicationId, service) -> service.modifyGuildApplicationCommand(
                        applicationId,
                        guild.getId().asLong(),
                        model.commandId().asLong(),
                        model.asRequest()
                      ));
                    }
                    return Mono.empty();
                  }
                })
                .then(event.editReply().withContentOrNull(Emoji.YES.asFormat()));
            });
        },
        REMOVE, option -> {
          return Mono.justOrEmpty(Options.string(option, OptionNames.NAME))
            .flatMap(name -> {
              return this.factoids.findByGuildAndName(guild.getId(), name)
                .switchIfEmpty(event.editReply().withContentOrNull("%s Could not find a factoid with name `%s`.".formatted(Emoji.NO.asFormat(), name)).then(Mono.empty()))
                .flatMap(model -> this.appAction((id, service) -> service.deleteGuildApplicationCommand(id, guild.getId().asLong(), model.commandId().asLong())).thenReturn(model))
                .flatMap(this.factoids::delete)
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
        return this.factoids.findAllByGuild(guild.getId())
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
    return Mono.just(1093035048015515668L).flatMap(applicationId -> consumer.apply(applicationId, this.rest.getApplicationService()));
  }
}
