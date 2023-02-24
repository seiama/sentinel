package com.seiama.sentinel.feature.punishment.command;

import com.seiama.sentinel.command.GuildCommand;
import com.seiama.sentinel.command.Options;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.common.model.PunishmentRepository;
import com.seiama.sentinel.feature.Feature;
import com.seiama.sentinel.feature.punishment.PunishmentAction;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class BanCommand implements GuildCommand {
  private static final String NAME = "ban";
  private final PunishmentRepository punishments;

  @Autowired
  private BanCommand(final PunishmentRepository punishments) {
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
      .description("Ban a member")
      .addOption(Options.option(option -> option.name(Options.MEMBER).description("The member to ban").type(ApplicationCommandOption.Type.USER.getValue()).required(true)))
      .addOption(Options.option(option -> option.name(Options.REASON).description("The reason for banning the member").type(ApplicationCommandOption.Type.STRING.getValue()).required(false)))
      .build();
  }

  @Override
  public @NotNull Feature feature() {
    return Feature.PUNISHMENTS;
  }

  @Override
  public @NotNull Mono<?> on(final @NotNull ChatInputInteractionEvent event, final @NotNull Guild guild) {
    return PunishmentAction.apply(
      event,
      guild,
      this.punishments,
      PunishmentModel.Type.BAN,
      PunishmentAction.BAN
    );
  }
}
