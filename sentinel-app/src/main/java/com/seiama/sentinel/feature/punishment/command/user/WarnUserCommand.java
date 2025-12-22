package com.seiama.sentinel.feature.punishment.command.user;

import com.seiama.sentinel.command.UserCommand;
import com.seiama.sentinel.common.discord.Modals;
import com.seiama.sentinel.common.model.Feature;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.feature.punishment.PunishmentAction;
import com.seiama.sentinel.feature.punishment.Punishments;
import com.seiama.sentinel.feature.punishment.creator.UserInteractionPunishmentCreator;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.UserInteractionEvent;
import discord4j.core.object.command.ApplicationCommand;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@NullMarked
public final class WarnUserCommand implements UserCommand {
  private final Punishments punishments;

  @Autowired
  public WarnUserCommand(final Punishments punishments) {
    this.punishments = punishments;
  }

  @Override
  public String name() {
    return "Warn";
  }

  @Override
  public ApplicationCommandRequest request() {
    return ApplicationCommandRequest.builder()
      .name(this.name())
      .type(ApplicationCommand.Type.USER.getValue())
      .defaultPermission(false)
      .build();
  }

  @Override
  public Feature feature() {
    return Feature.PUNISHMENTS;
  }

  @Override
  public Mono<?> on(final GatewayDiscordClient client, final UserInteractionEvent event, final Guild guild) {
    final User user = event.getResolvedUser();
    return Modals.presentAndCaptureSingleTextInput(
      event,
      "Warn " + user.getTag(),
      "Reason",
      true,
      (modal, reason) -> this.punishments.createUsing(new UserInteractionPunishmentCreator(
        client,
        modal,
        guild,
        PunishmentModel.Type.WARN,
        null,
        PunishmentAction.warn(),
        user,
        reason.orElse(null)
      )),
      null
    );
  }
}
