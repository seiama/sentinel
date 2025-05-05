package com.seiama.sentinel.feature.punishment.command;

import com.google.common.collect.Lists;
import com.seiama.sentinel.command.Command;
import com.seiama.sentinel.command.GuildCommand;
import com.seiama.sentinel.command.OptionNames;
import com.seiama.sentinel.command.Options;
import com.seiama.sentinel.common.bson.AsObjectId;
import com.seiama.sentinel.common.discord.Pagination;
import com.seiama.sentinel.common.model.Feature;
import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.common.model.PunishmentRepository;
import com.seiama.sentinel.feature.punishment.Punishments;
import com.seiama.sentinel.feature.punishment.display.PunishmentDisplay;
import com.seiama.sentinel.feature.punishment.display.PunishmentDisplayStyle;
import com.seiama.sentinel.feature.punishment.display.PunishmentMessages;
import com.seiama.sentinel.feature.punishment.predicate.CanQueryAndMutate;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

@Component
@NullMarked
public final class PunishmentCommand implements GuildCommand {
  private static final String SEARCH = "search";
  private static final String USER = "user";
  private static final String SHOW = "show";
  private static final String REASON = "reason";
  private static final String STALE = "stale";

  private final GuildRepository guilds;
  private final Punishments punishmentOps;
  private final PunishmentRepository punishments;

  @Autowired
  private PunishmentCommand(final GuildRepository guilds, final Punishments punishmentOps, final PunishmentRepository punishments) {
    this.guilds = guilds;
    this.punishmentOps = punishmentOps;
    this.punishments = punishments;
  }

  @Override
  public String name() {
    return "punishment";
  }

  @Override
  public ApplicationCommandRequest request() {
    return ApplicationCommandRequest.builder()
      .name(this.name())
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
                  .name(OptionNames.USER)
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
              .name(OptionNames.PUNISHMENT)
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
              .name(OptionNames.PUNISHMENT)
              .description("The id of the punishment to modify")
              .required(true)
              .type(ApplicationCommandOption.Type.STRING.getValue())
              .build()
          )
          .addOption(
            ApplicationCommandOptionData.builder()
              .name(OptionNames.REASON)
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
              .name(OptionNames.PUNISHMENT)
              .description("The id of the punishment to mark as stale")
              .required(true)
              .type(ApplicationCommandOption.Type.STRING.getValue())
              .build()
          )
          .addOption(
            ApplicationCommandOptionData.builder()
              .name(OptionNames.REASON)
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
  public Feature feature() {
    return Feature.PUNISHMENTS;
  }

  @Override
  public Mono<?> on(final GatewayDiscordClient client, final ChatInputInteractionEvent event, final Guild guild) {
    final Member punisher = event.getInteraction().getMember().orElseThrow();
    return event.deferReply().then(Command.executeOne(event, Map.of(
      SHOW, option -> {
        return Mono.justOrEmpty(Options.string(option, OptionNames.PUNISHMENT).orElse(null))
          .filterWhen(new CanQueryAndMutate<>(this.guilds, guild, punisher))
          .handle(AsObjectId.INSTANCE)
          .flatMap(this.punishments::findById)
          .flatMap(punishment -> event.editReply().withComponents(PunishmentDisplay.punishment(punishment, PunishmentDisplayStyle.FULL)))
          .onErrorResume(throwable -> event.editReply().withContentOrNull(PunishmentMessages.PUNISHMENT_NOT_FOUND));
      },
      REASON, option -> {
        return Mono.justOrEmpty(Options.string(option, OptionNames.PUNISHMENT).orElse(null))
          .filterWhen(new CanQueryAndMutate<>(this.guilds, guild, punisher))
          .handle(AsObjectId.INSTANCE)
          .zipWith(Mono.justOrEmpty(Options.string(option, OptionNames.REASON).orElse(null)))
          .flatMap(TupleUtils.function((id, reason) -> {
            return this.punishments.findById(id)
              .flatMap(model -> this.punishments.update(model, new PunishmentModel.Partial.Reason() {
                @Override
                public @Nullable String reason() {
                  return reason;
                }
              }));
          }))
          .flatMap(result -> event.editReply().withComponents(PunishmentDisplay.updated(result)));
      },
      STALE, option -> {
        return Mono.justOrEmpty(Options.string(option, OptionNames.PUNISHMENT).orElse(null))
          .filterWhen(new CanQueryAndMutate<>(this.guilds, guild, punisher))
          .handle(AsObjectId.INSTANCE)
          .zipWith(Mono.just(Options.string(option, OptionNames.REASON)))
          .flatMap(TupleUtils.function((id, reason) -> {
            return this.punishments.findById(id)
              .flatMap(model -> this.punishments.update(model, PunishmentModel.Partial.Stale.of(Optional.of(punisher), reason.orElse(null), false, null)))
              .flatMap(model -> this.punishmentOps.unenforce(guild, model, String.format("Punishment (%s) has been marked stale.", model._id())).thenReturn(model));
          }))
          .flatMap(result -> event.editReply().withComponents(
            PunishmentDisplay.punishment(result, PunishmentDisplayStyle.FULL),
            PunishmentDisplay.updated(result)
          ));
      },
      SEARCH, option -> {
        return Mono.justOrEmpty(option.getOption(USER).orElse(null))
          .filterWhen(new CanQueryAndMutate<>(this.guilds, guild, punisher))
          .flatMap(user -> Options.user(user, OptionNames.USER).orElseGet(Mono::empty))
          .flatMap(user -> this.punishments.findAllByGuildAndPunishedIdOrderByDateDesc(guild.getId(), user.getId()).collectList().zipWith(Mono.just(user)))
          .flatMap(TupleUtils.function((punishment, user) -> this.onSearch(client, event, user, punishment)));
      }
    )));
  }

  private Mono<Void> onSearch(final GatewayDiscordClient client, final ChatInputInteractionEvent event, final User user, final List<PunishmentModel.Complete> punishments) {
    final boolean paginate = punishments.size() > PunishmentDisplay.HISTORY_PAGE_SIZE;
    if (paginate) {
      final List<List<PunishmentModel.Complete>> pages = Lists.partition(punishments, PunishmentDisplay.HISTORY_PAGE_SIZE);
      final int size = pages.size();
      final Pagination pagination = new Pagination(size, (button, page, buttons) -> {
        return button.editReply()
          .withComponents(
            PunishmentDisplay.history(user, pages.get(page)),
            buttons.get()
          )
          .then();
      });
      return Mono.when(
        pagination.createListener(client),
        event.editReply()
          .withComponents(
            PunishmentDisplay.history(user, pages.get(0)),
            pagination.createButtons()
          )
      );
    } else {
      return event.editReply().withComponents(PunishmentDisplay.history(user, punishments))
        .then();
    }
  }
}
