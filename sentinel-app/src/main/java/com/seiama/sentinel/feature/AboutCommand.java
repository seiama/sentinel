package com.seiama.sentinel.feature;

import com.seiama.sentinel.Sentinel;
import com.seiama.sentinel.command.GlobalCommand;
import discord4j.common.util.TimestampFormat;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.Color;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public final class AboutCommand implements GlobalCommand {
  private static final String NAME = "about";
  private final GitProperties git;

  @Autowired
  private AboutCommand(final GitProperties git) {
    this.git = git;
  }

  @Override
  public @NotNull String name() {
    return NAME;
  }

  @Override
  public @NotNull ApplicationCommandRequest request() {
    return ApplicationCommandRequest.builder()
      .name(NAME)
      .description("Displays information about Sentinel")
      .build();
  }

  @Override
  public @NotNull Mono<?> on(final @NotNull GatewayDiscordClient client, final @NotNull ChatInputInteractionEvent event) {
    return client.getSelf().flatMap(user -> {
      final EmbedCreateSpec embed = EmbedCreateSpec.builder()
        .color(Color.of(0xec4768))
        .thumbnail(user.getAvatarUrl())
        .title("Sentinel")
        .addField("Version", String.format(
          "`%s` on `%s`",
          this.git.getShortCommitId(),
          this.git.getBranch()
        ), false)
        .addField("Build Time", TimestampFormat.LONG_DATE_TIME.format(this.git.getCommitTime()), false)
        .addField("Boot Time", TimestampFormat.RELATIVE_TIME.format(Sentinel.BOOT_TIME), false)
        .build();
      return event.reply()
        .withEmbeds(embed);
    });
  }
}
