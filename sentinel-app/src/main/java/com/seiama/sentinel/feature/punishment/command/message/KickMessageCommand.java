package com.seiama.sentinel.feature.punishment.command.message;

import com.seiama.sentinel.command.MessageCommand;
import com.seiama.sentinel.common.discord.Modals;
import com.seiama.sentinel.common.model.Feature;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.feature.punishment.PunishmentAction;
import com.seiama.sentinel.feature.punishment.Punishments;
import com.seiama.sentinel.feature.punishment.creator.MessageInteractionPunishmentCreator;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import discord4j.core.object.command.ApplicationCommand;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class KickMessageCommand implements MessageCommand {
  private static final String NAME = "Kick";
  private final Punishments punishments;

  @Autowired
  private KickMessageCommand(final Punishments punishments) {
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
      .type(ApplicationCommand.Type.MESSAGE.getValue())
      .defaultPermission(false)
      .build();
  }

  @Override
  public @NotNull Feature feature() {
    return Feature.PUNISHMENTS;
  }

  @Override
  public @NotNull Mono<?> on(final @NotNull GatewayDiscordClient client, final @NotNull MessageInteractionEvent event, final @NotNull Guild guild) {
    final String modalTitle = "Kick" + event.getResolvedMessage().getAuthor().map(User::getTag).map(" "::concat).orElse("");
    return Modals.presentAndCaptureSingleTextInput(event, modalTitle, "Reason", false, (modal, reason) -> {
      return this.punishments.createUsing(new MessageInteractionPunishmentCreator(modal, event.getResolvedMessage(), guild, PunishmentModel.Type.KICK, PunishmentAction.kick(), reason.orElse(null)));
    });
  }
}
