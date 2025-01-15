package com.seiama.sentinel.feature.punishment.appeal;

import com.seiama.sentinel.command.Command;
import com.seiama.sentinel.command.GuildCommand;
import com.seiama.sentinel.command.Options;
import com.seiama.sentinel.common.discord.Emoji;
import com.seiama.sentinel.common.model.Feature;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

@Component
public final class AppealCommand implements GuildCommand {
  private static final String NAME = "appeal";

  private static final String ACCEPT = "accept";
  private static final String DENY = "deny";

  private final Appeals appeals;

  @Autowired
  private AppealCommand(final Appeals appeals) {
    this.appeals = appeals;
  }

  @Override
  public @NotNull String name() {
    return NAME;
  }

  @Override
  public @NotNull ApplicationCommandRequest request() {
    return ApplicationCommandRequest.builder()
      .name(NAME)
      .description("Manage punishment appeals")
      .defaultPermission(false)
      .addOption(
        ApplicationCommandOptionData.builder()
          .name(ACCEPT)
          .description("Accept a punishment appeal")
          .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
          .build()
      )
      .addOption(
        ApplicationCommandOptionData.builder()
          .name(DENY)
          .description("Deny a punishment appeal")
          .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
          .addOption(
            ApplicationCommandOptionData.builder()
              .name(Options.REASON)
              .description("The reason for denying the punishment appeal")
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
    return Feature.PUNISHMENTS_APPEALS;
  }

  @Override
  public @NotNull Mono<?> on(final @NotNull GatewayDiscordClient client, final @NotNull ChatInputInteractionEvent event, final @NotNull Guild guild) {
    final Interaction interaction = event.getInteraction();
    return event.deferReply().then(Command.executeOne(event, Map.of(
      ACCEPT, option -> {
        return this.appeals.findByAppealThread(interaction.getChannelId())
          .switchIfEmpty(event.editReply().withContentOrNull(Appeals.NO_APPEAL_ASSOCIATED_WITH_THIS_CHANNEL).then(Mono.empty()))
          .flatMap(model -> this.appeals.accept(client, model, interaction.getUser()))
          .then(event.editReply().withContentOrNull(Emoji.YES.asFormat() + " Successfully accepted appeal."));
      },
      DENY, option -> {
        return Mono.just(Options.string(option, Options.REASON))
          .zipWith(this.appeals.findByAppealThread(interaction.getChannelId()))
          .switchIfEmpty(event.editReply().withContentOrNull(Appeals.NO_APPEAL_ASSOCIATED_WITH_THIS_CHANNEL).then(Mono.empty()))
          .flatMap(TupleUtils.function((reason, model) -> this.appeals.deny(client, model, interaction.getUser(), reason.orElse(null), null)))
          .then(event.editReply().withContentOrNull(Emoji.YES.asFormat() + " Successfully denied appeal."));
      }
    )));
  }
}
