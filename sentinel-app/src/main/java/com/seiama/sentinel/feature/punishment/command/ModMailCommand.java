package com.seiama.sentinel.feature.punishment.command;

import com.seiama.sentinel.command.GuildCommand;
import com.seiama.sentinel.command.OptionNames;
import com.seiama.sentinel.common.model.Feature;
import com.seiama.sentinel.common.model.ModMailModel;
import com.seiama.sentinel.feature.punishment.ModMail;
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
public class ModMailCommand implements GuildCommand {
  private final ModMail modmail;

  @Autowired
  public ModMailCommand(final ModMail modmail) {
    this.modmail = modmail;
  }

  @Override
  public String name() {
    return "modmail";
  }

  @Override
  public ApplicationCommandRequest request() {
    return ApplicationCommandRequest.builder()
      .name(this.name())
      .description("Sends a message privately to the moderators")
      .defaultPermission(false)
      .addOption(
        ApplicationCommandOptionData.builder()
          .name(OptionNames.MESSAGE)
          .description("The message to send the moderators")
          .required(true)
          .type(ApplicationCommandOption.Type.STRING.getValue())
          .build()
      )
      .build();
  }

  @Override
  public Feature feature() {
    return Feature.MODMAIL;
  }

  @Override
  public Mono<?> on(final GatewayDiscordClient client, final ChatInputInteractionEvent event, final Guild guild) {
    return event.deferReply()
      .withEphemeral(true)
      .then(this.modmail.create(
        client,
        guild,
        event.getInteraction().getUser(),
        ModMailModel.Type.MODMAIL,
        null,
        event.getOptionAsString(OptionNames.MESSAGE).orElseThrow()
      ))
      .then(event.editReply().withContentOrNull("Your message has been successfully sent."));
  }
}
