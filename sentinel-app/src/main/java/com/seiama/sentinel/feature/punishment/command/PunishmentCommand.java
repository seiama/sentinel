package com.seiama.sentinel.feature.punishment.command;

import com.seiama.sentinel.command.Command;
import com.seiama.sentinel.command.GuildCommand;
import com.seiama.sentinel.command.Options;
import com.seiama.sentinel.common.bson.AsObjectId;
import com.seiama.sentinel.common.model.Feature;
import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.common.model.PunishmentRepository;
import com.seiama.sentinel.feature.punishment.display.PunishmentDisplay;
import com.seiama.sentinel.feature.punishment.display.PunishmentDisplayStyle;
import com.seiama.sentinel.feature.punishment.display.PunishmentMessages;
import com.seiama.sentinel.feature.punishment.predicate.CanQueryAndMutate;
import com.seiama.sentinel.feature.punishment.search.PunishmentSearchResult;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

@Component
public final class PunishmentCommand implements GuildCommand {
  private static final String NAME = "punishment";

  private static final String SEARCH = "search";
  private static final String USER = "user";
  private static final String SHOW = "show";
  private static final String REASON = "reason";
  private static final String STALE = "stale";

  private final GuildRepository guilds;
  private final PunishmentRepository punishments;

  @Autowired
  private PunishmentCommand(final GuildRepository guilds, final PunishmentRepository punishments) {
    this.guilds = guilds;
    this.punishments = punishments;
  }

  @Override
  public @NotNull String name() {
    return NAME;
  }

  @Override
  public @NotNull ApplicationCommandRequest request() {
    return ApplicationCommandRequest.builder()
      .name(NAME)
      .description("Query and manage punishments")
      .defaultPermission(false)
      .addOption(
        ApplicationCommandOptionData.builder()
          .name(SEARCH)
          .description("Search issued punishments")
          .type(ApplicationCommandOption.Type.SUB_COMMAND_GROUP.getValue())
          .addOption(
            ApplicationCommandOptionData.builder()
              .name(USER)
              .description("Search for punishments issued to a user")
              .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
              .addOption(
                ApplicationCommandOptionData.builder()
                  .name(Options.USER)
                  .description("The user")
                  .required(true)
                  .type(ApplicationCommandOption.Type.USER.getValue())
                  .build()
              )
              .build()
          )
          .build()
      )
      .addOption(
        ApplicationCommandOptionData.builder()
          .name(SHOW)
          .description("Show the details of a punishment")
          .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
          .addOption(
            ApplicationCommandOptionData.builder()
              .name(Options.PUNISHMENT)
              .description("The id of the punishment to show")
              .required(true)
              .type(ApplicationCommandOption.Type.STRING.getValue())
              .build()
          )
          .build()
      )
      .addOption(
        ApplicationCommandOptionData.builder()
          .name(REASON)
          .description("Set the reason of a punishment")
          .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
          .addOption(
            ApplicationCommandOptionData.builder()
              .name(Options.PUNISHMENT)
              .description("The id of the punishment to modify")
              .required(true)
              .type(ApplicationCommandOption.Type.STRING.getValue())
              .build()
          )
          .addOption(
            ApplicationCommandOptionData.builder()
              .name(Options.REASON)
              .description("The new reason")
              .required(true)
              .type(ApplicationCommandOption.Type.STRING.getValue())
              .build()
          )
          .build()
      )
      .addOption(
        ApplicationCommandOptionData.builder()
          .name(STALE)
          .description("Mark a punishment stale")
          .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
          .addOption(
            ApplicationCommandOptionData.builder()
              .name(Options.PUNISHMENT)
              .description("The id of the punishment to mark as stale")
              .required(true)
              .type(ApplicationCommandOption.Type.STRING.getValue())
              .build()
          )
          .addOption(
            ApplicationCommandOptionData.builder()
              .name(Options.REASON)
              .description("The reason for marking this punishment stale")
              .required(false)
              .type(ApplicationCommandOption.Type.STRING.getValue())
              .build()
          )
          .build()
      )
      .build();
  }

  @Override
  public @NotNull Feature feature() {
    return Feature.PUNISHMENTS;
  }

  @Override
  public @NotNull Mono<?> on(final @NotNull GatewayDiscordClient client, final @NotNull ChatInputInteractionEvent event, final @NotNull Guild guild) {
    final Member punisher = event.getInteraction().getMember().orElseThrow();
    return event.deferReply().then(Command.executeOne(event, Map.of(
      SHOW, option -> {
        return Mono.justOrEmpty(Options.string(option, Options.PUNISHMENT).orElse(null))
          .filterWhen(new CanQueryAndMutate<>(this.guilds, guild, punisher))
          .handle(AsObjectId.INSTANCE)
          .flatMap(this.punishments::findById)
          .flatMap(punishment -> event.editReply().withEmbeds(PunishmentDisplay.punishment(punishment, PunishmentDisplayStyle.FULL)))
          .onErrorResume(throwable -> event.editReply().withContentOrNull(PunishmentMessages.PUNISHMENT_NOT_FOUND));
      },
      REASON, option -> {
        return Mono.justOrEmpty(Options.string(option, Options.PUNISHMENT).orElse(null))
          .filterWhen(new CanQueryAndMutate<>(this.guilds, guild, punisher))
          .handle(AsObjectId.INSTANCE)
          .zipWith(Mono.justOrEmpty(Options.string(option, Options.REASON).orElse(null)))
          .flatMap(TupleUtils.function((id, reason) -> {
            return this.punishments.findById(id)
              .flatMap(model -> this.punishments.update(model, new PunishmentModel.Partial.Reason() {
                @Override
                public @Nullable String reason() {
                  return reason;
                }
              }));
          }))
          .flatMap(result -> event.editReply().withContentOrNull(PunishmentMessages.punishmentUpdated(result)));
      },
      STALE, option -> {
        return Mono.justOrEmpty(Options.string(option, Options.PUNISHMENT).orElse(null))
          .filterWhen(new CanQueryAndMutate<>(this.guilds, guild, punisher))
          .handle(AsObjectId.INSTANCE)
          .zipWith(Mono.just(Options.string(option, Options.REASON)))
          .flatMap(TupleUtils.function((id, reason) -> {
            return this.punishments.findById(id)
              .flatMap(model -> this.punishments.update(model, PunishmentModel.Partial.Stale.of(punisher, reason.orElse(null), false, null)));
          }))
          .flatMap(result -> event.editReply().withContentOrNull(PunishmentMessages.punishmentMarkedStale(result)).withEmbeds(PunishmentDisplay.punishment(result, PunishmentDisplayStyle.FULL)));
      },
      SEARCH, option -> {
        return Mono.justOrEmpty(option.getOption(USER).orElse(null))
          .filterWhen(new CanQueryAndMutate<>(this.guilds, guild, punisher))
          .flatMap(user -> Options.user(user, Options.USER).orElseGet(Mono::empty))
          .flatMap(user -> this.punishments.findAllByPunishedId(user.getId()).collectList().zipWith(Mono.just(user)))
          .map(TupleUtils.function((punishment, user) -> new PunishmentSearchResult(user, punishment)))
          .flatMap(result -> event.editReply().withEmbeds(PunishmentMessages.punishmentSearchEmbed(result)));
      }
    )));
  }
}
