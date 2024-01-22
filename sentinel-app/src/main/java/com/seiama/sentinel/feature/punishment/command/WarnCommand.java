package com.seiama.sentinel.feature.punishment.command;

import com.seiama.sentinel.command.GuildCommand;
import com.seiama.sentinel.command.OptionNames;
import com.seiama.sentinel.common.model.Feature;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.feature.punishment.PunishmentAction;
import com.seiama.sentinel.feature.punishment.Punishments;
import com.seiama.sentinel.feature.punishment.creator.ChatInteractionPunishmentCreator;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class WarnCommand implements GuildCommand {
  private static final String NAME = "warn";
  private final Punishments punishments;

  @Autowired
  private WarnCommand(final Punishments punishments) {
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
      .description("Warn a member")
      .defaultPermission(false)
      .addOption(
        ApplicationCommandOptionData.builder()
          .name(OptionNames.MEMBER)
          .description("The member to warn")
          .type(ApplicationCommandOption.Type.USER.getValue())
          .required(true)
          .build()
      )
      .addOption(
        ApplicationCommandOptionData.builder()
          .name(OptionNames.REASON)
          .description("The reason for warning the member")
          .type(ApplicationCommandOption.Type.STRING.getValue())
          .required(true)
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
    return this.punishments.createUsing(new ChatInteractionPunishmentCreator(event, guild, PunishmentModel.Type.WARN, PunishmentAction.warn()));
  }
}
