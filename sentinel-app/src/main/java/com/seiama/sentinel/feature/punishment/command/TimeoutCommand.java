package com.seiama.sentinel.feature.punishment.command;

import com.seiama.sentinel.command.GuildCommand;
import com.seiama.sentinel.command.OptionNames;
import com.seiama.sentinel.common.model.Feature;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.feature.punishment.PunishmentAction;
import com.seiama.sentinel.feature.punishment.Punishments;
import com.seiama.sentinel.feature.punishment.TimeoutDuration;
import com.seiama.sentinel.feature.punishment.creator.ChatInteractionPunishmentCreator;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.core.object.entity.Guild;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@NullMarked
public final class TimeoutCommand implements GuildCommand {
  private final Punishments punishments;

  @Autowired
  private TimeoutCommand(final Punishments punishments) {
    this.punishments = punishments;
  }

  @Override
  public String name() {
    return "timeout";
  }

  @Override
  public ApplicationCommandRequest request() {
    return ApplicationCommandRequest.builder()
      .name(this.name())
      .description("Timeout a member")
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
          .description("The reason for timing out the member")
          .type(ApplicationCommandOption.Type.STRING.getValue())
          .required(true)
          .build()
      )
      .addOption(
        ApplicationCommandOptionData.builder()
          .name(OptionNames.DURATION)
          .description("The duration")
          .type(ApplicationCommandOption.Type.STRING.getValue())
          .choices(
            Arrays.stream(TimeoutDuration.values())
              .map(option -> {
                return ApplicationCommandOptionChoiceData.builder()
                  .name(option.description())
                  .value(option.name())
                  .build();
              })
              .toList()
          )
          .required(true)
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
    final Duration duration = event.getOptionAsString(OptionNames.DURATION)
      .map(TimeoutDuration::valueOf)
      .map(TimeoutDuration::duration)
      .orElseThrow();
    final Instant startsAt = Instant.now();
    final Instant endsAt = startsAt.plus(duration);
    return this.punishments.createUsing(new ChatInteractionPunishmentCreator(
      client,
      event,
      guild,
      PunishmentModel.Type.MUTE,
      Duration.between(startsAt, endsAt),
      PunishmentAction.mute(endsAt)
    ));
  }
}
