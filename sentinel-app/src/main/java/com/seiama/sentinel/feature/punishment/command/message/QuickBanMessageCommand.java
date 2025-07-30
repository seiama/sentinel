package com.seiama.sentinel.feature.punishment.command.message;

import com.seiama.sentinel.command.MessageCommand;
import com.seiama.sentinel.common.model.Feature;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.feature.punishment.PunishmentAction;
import com.seiama.sentinel.feature.punishment.Punishments;
import com.seiama.sentinel.feature.punishment.creator.MessageInteractionPunishmentCreator;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import discord4j.core.object.command.ApplicationCommand;
import discord4j.core.object.entity.Guild;
import discord4j.core.util.MentionUtil;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@NullMarked
public final class QuickBanMessageCommand implements MessageCommand {
  private final Punishments punishments;

  @Autowired
  private QuickBanMessageCommand(final Punishments punishments) {
    this.punishments = punishments;
  }

  @Override
  public String name() {
    return "Quick Ban";
  }

  @Override
  public ApplicationCommandRequest request() {
    return ApplicationCommandRequest.builder()
      .name(this.name())
      .type(ApplicationCommand.Type.MESSAGE.getValue())
      .defaultPermission(false)
      .build();
  }

  @Override
  public Feature feature() {
    return Feature.PUNISHMENTS;
  }

  @Override
  public Mono<?> on(final GatewayDiscordClient client, final MessageInteractionEvent event, final Guild guild) {
    final String reason = "Quick-banned for sending a message in " + MentionUtil.forChannel(event.getResolvedMessage().getChannelId());
    return this.punishments.createUsing(new MessageInteractionPunishmentCreator(client, event, guild, PunishmentModel.Type.BAN, null, PunishmentAction.ban(true), event.getResolvedMessage(), reason));
  }
}
