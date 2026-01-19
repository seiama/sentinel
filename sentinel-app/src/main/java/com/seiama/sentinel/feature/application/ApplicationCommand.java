package com.seiama.sentinel.feature.application;

import com.seiama.sentinel.command.GuildCommand;
import com.seiama.sentinel.common.model.Feature;
import com.seiama.sentinel.common.model.GuildRepository;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import static com.seiama.sentinel.feature.application.ApplicationListener.OPEN_MODAL_BUTTON_ID;

@Component
@NullMarked
public final class ApplicationCommand implements GuildCommand {
  private static final String NAME = "application";

  private final GuildRepository guilds;

  @Autowired
  public ApplicationCommand(GuildRepository guilds) {
    this.guilds = guilds;
  }

  @Override
  public String name() {
    return NAME;
  }

  @Override
  public ApplicationCommandRequest request() {
    return ApplicationCommandRequest.builder()
      .name(NAME)
      .description("Add application form to this channel.")
      .defaultPermission(false)
      .build();
  }

  @Override
  public Feature feature() {
    return Feature.APPLICATION;
  }

  @Override
  public Mono<?> on(final GatewayDiscordClient client, final ChatInputInteractionEvent event, final Guild guild) {
    return event.deferReply()
      .withEphemeral(true)
      .then(this.guilds.findByGuild(guild.getId())
        .flatMap(guildConfig -> {
          if (!Feature.APPLICATION.enabledForGuild(guildConfig)) {
            return Mono.empty();
          }
          return guild.getChannelById(guildConfig.features().application().applicationChannel())
            .cast(TextChannel.class)
            .flatMap(channel ->
              channel.createMessage(EmbedCreateSpec.builder()
                  .title("Apply as contributor")
                  .description("If you are interested in continued, non trivial contributions to Paper, please fill out this application form to get access to #paper-contrib for discussions and collaboration with the team and other contributors.")
                  .build())
                .then(channel.createMessage(MessageCreateSpec.builder()
                  .components(ActionRow.of(
                    Button.primary(OPEN_MODAL_BUTTON_ID, "Apply")
                  ))
                  .build()))
            );
        })
      )
      .then(event.editReply().withContentOrNull("Application send, we will review your application within a few days."))
      .then();
  }
}
