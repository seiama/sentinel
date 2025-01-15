package com.seiama.sentinel.command;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.seiama.sentinel.common.Listener;
import com.seiama.sentinel.common.model.GuildRepository;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.service.ApplicationService;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
class Commands implements Listener {
  private final long applicationId;
  private final GuildRepository guilds;
  private final ApplicationService applicationService;
  private final Set<GlobalCommand> globalCommands;
  private final Set<GuildCommand> guildCommands;
  private final Map<String, GlobalCommand> globalCommandsByName = new HashMap<>();
  private final Table<Snowflake, String, GuildCommand> guildCommandsByGuildAndName = HashBasedTable.create();

  @Autowired
  Commands(final @Qualifier("applicationId") long applicationId, final GuildRepository guilds, final ApplicationService applicationService, final Set<GlobalCommand> globalCommands, final Set<GuildCommand> guildCommands) {
    this.applicationId = applicationId;
    this.guilds = guilds;
    this.applicationService = applicationService;
    this.globalCommands = globalCommands;
    this.guildCommands = guildCommands;
  }

  @Override
  public void connected(final @NotNull GatewayDiscordClient client) {
    Flux.fromIterable(this.globalCommands)
      .doOnNext(command -> this.globalCommandsByName.put(command.name(), command))
      .map(Command::request)
      .collectList()
      .flatMapMany(requests -> this.applicationService.bulkOverwriteGlobalApplicationCommand(this.applicationId, requests))
      .subscribe();

    this.guilds.findAll()
      .map(model -> {
        final Set<GuildCommand> commands = new HashSet<>();
        for (final GuildCommand command : this.guildCommands) {
          if (command.feature().enabledForGuild(model)) {
            this.guildCommandsByGuildAndName.put(model.guild(), command.name(), command);
            commands.add(command);
          }
        }
        return new GuildRequests(
          model.guild().asLong(),
          commands
            .stream()
            .map(Command::request)
            .toList()
        );
      })
      .flatMap(requests -> this.applicationService.bulkOverwriteGuildApplicationCommand(this.applicationId, requests.guild(), requests.requests()))
      .subscribe();
  }

  @Override
  @SuppressWarnings("CodeBlock2Expr") // readability
  public @NotNull Mono<Void> listen(final @NotNull GatewayDiscordClient client) {
    return Mono.when(
      client.on(ChatInputInteractionEvent.class, event -> {
        return Mono.when(
          Mono.defer(() -> {
            return event.getInteraction().getGuild()
              .flatMap(guild -> {
                return Mono.justOrEmpty(this.guildCommandsByGuildAndName.get(guild.getId(), event.getCommandName()))
                  .flatMap(command -> command.on(client, event, guild));
              });
          }),
          Mono.defer(() -> {
            return event.getInteraction().getGuild()
              .flatMap(guild -> {
                return Mono.justOrEmpty(this.globalCommandsByName.get(event.getCommandName()))
                  .flatMap(command -> command.on(client, event));
              });
          })
        );
      }),
      client.on(ChatInputAutoCompleteEvent.class, event -> {
        return Mono.when(
          Mono.defer(() -> {
            return event.getInteraction().getGuild()
              .flatMap(guild -> {
                return Mono.justOrEmpty(this.guildCommandsByGuildAndName.get(guild.getId(), event.getCommandName()))
                  .flatMap(command -> command.suggest(client, event, guild));
              });
          }),
          Mono.defer(() -> {
            return event.getInteraction().getGuild()
              .flatMap(guild -> {
                return Mono.justOrEmpty(this.globalCommandsByName.get(event.getCommandName()))
                  .flatMap(command -> command.suggest(client, event));
              });
          })
        );
      })
    );
  }

  private record GuildRequests(
    long guild,
    List<ApplicationCommandRequest> requests
  ) {
  }
}
