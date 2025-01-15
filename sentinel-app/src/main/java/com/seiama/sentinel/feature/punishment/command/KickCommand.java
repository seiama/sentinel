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
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@NullMarked
public final class KickCommand implements GuildCommand {
  private static final String NAME = "kick";
  private final Punishments punishments;

  @Autowired
  private KickCommand(final Punishments punishments) {
    this.punishments = punishments;
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public ApplicationCommandRequest request() {
    return ApplicationCommandRequest.builder()
      .name(NAME)
      .description("Kick a member")
      .defaultPermission(false)
      .addOption(
        ApplicationCommandOptionData.builder()
          .name(OptionNames.MEMBER)
          .description("The member to kick")
          .type(ApplicationCommandOption.Type.USER.getValue())
          .required(true)
          .build()
      )
      .addOption(
        ApplicationCommandOptionData.builder()
          .name(OptionNames.REASON)
          .description("The reason for kicking the member")
          .type(ApplicationCommandOption.Type.STRING.getValue())
          .required(false)
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
    return this.punishments.createUsing(new ChatInteractionPunishmentCreator(client, event, guild, PunishmentModel.Type.KICK, PunishmentAction.kick()));
  }
}
