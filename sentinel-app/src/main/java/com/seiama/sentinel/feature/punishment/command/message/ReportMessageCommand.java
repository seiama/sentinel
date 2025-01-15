package com.seiama.sentinel.feature.punishment.command.message;

import com.seiama.sentinel.command.MessageCommand;
import com.seiama.sentinel.common.model.Feature;
import com.seiama.sentinel.common.model.ModMailModel;
import com.seiama.sentinel.feature.punishment.ModMail;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import discord4j.core.object.command.ApplicationCommand;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@NullMarked
public final class ReportMessageCommand implements MessageCommand {
  private static final String NAME = "Report";

  private final ModMail modmail;

  @Autowired
  private ReportMessageCommand(final ModMail modmail) {
    this.modmail = modmail;
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public ApplicationCommandRequest request() {
    return ApplicationCommandRequest.builder()
      .name(NAME)
      .type(ApplicationCommand.Type.MESSAGE.getValue())
      .defaultPermission(false)
      .build();
  }

  @Override
  public Feature feature() {
    return Feature.MODMAIL;
  }

  @Override
  public Mono<?> on(final GatewayDiscordClient client, final MessageInteractionEvent event, final Guild guild) {
    return event.deferReply()
      .withEphemeral(true)
      .then(this.modmail.create(
        client,
        guild,
        event.getInteraction().getUser(),
        ModMailModel.Type.REPORT,
        event.getResolvedMessage()
      ))
      .then(event.editReply("Your report has been successfully submitted."));
  }
}
