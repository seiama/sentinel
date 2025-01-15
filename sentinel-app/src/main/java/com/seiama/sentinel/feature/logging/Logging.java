package com.seiama.sentinel.feature.logging;

import com.seiama.common.Comparables;
import com.seiama.sentinel.common.Listener;
import com.seiama.sentinel.common.discord.Emoji;
import com.seiama.sentinel.common.discord.Mention;
import com.seiama.sentinel.common.model.GuildModel;
import com.seiama.sentinel.common.model.GuildRepository;
import discord4j.common.util.TimestampFormat;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.TextChannel;
import java.time.Duration;
import java.time.Instant;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class Logging implements Listener {
  private static final Duration NEW_THRESHOLD = Duration.ofDays(7);
  private final GuildRepository guilds;

  @Autowired
  public Logging(final GuildRepository guilds) {
    this.guilds = guilds;
  }

  @Override
  public @NotNull Mono<Void> listen(final @NotNull GatewayDiscordClient client) {
    return Mono.when(
      client.on(MemberJoinEvent.class, event -> {
        return event.getGuild()
          .flatMapMany(guild -> {
            return this.channelsFor(guild, GuildModel.Complete.Features.Logging.Event.MEMBER_JOIN)
              .flatMap(channel -> {
                final Member member = event.getMember();
                final StringBuilder sb = new StringBuilder();
                sb
                  .append(Emoji.DOT_GREEN.asFormat())
                  .append(' ')
                  .append(Mention.userWithId(member))
                  .append(" joined - created ");
                final Instant userCreation = member.getId().getTimestamp();
                sb.append(TimestampFormat.RELATIVE_TIME.format(userCreation));
                if (Comparables.lessThanOrEqual(Duration.between(userCreation, Instant.now()), NEW_THRESHOLD)) {
                  sb.append(' ').append(Emoji.DOT_ORANGE.asFormat());
                }
                return channel.createMessage(sb.toString());
              });
          });
      }),
      client.on(MemberLeaveEvent.class, event -> {
        return event.getGuild()
          .flatMapMany(guild -> {
            return this.channelsFor(guild, GuildModel.Complete.Features.Logging.Event.MEMBER_LEAVE)
              .flatMap(channel -> {
                final StringBuilder sb = new StringBuilder();
                sb
                  .append(Emoji.DOT_RED.asFormat())
                  .append(' ')
                  .append(Mention.userWithId(event.getUser()))
                  .append(" left");
                final Member member = event.getMember().orElse(null);
                if (member != null && member.getJoinTime().isPresent()) {
                  sb.append(" - joined ").append(TimestampFormat.RELATIVE_TIME.format(member.getJoinTime().get()));
                }
                return channel.createMessage(sb.toString());
              });
          });
      })
    );
  }

  private Flux<TextChannel> channelsFor(final Guild guild, final GuildModel.Complete.Features.Logging.Event event) {
    return this.guilds.findByGuild(guild.getId())
      .map(model -> model.features().logging())
      .filter(GuildModel.Complete.Features.Logging::enabled)
      .flatMapMany(config -> {
        return Flux.fromIterable(config.mapping().entrySet())
          .filter(entry -> entry.getValue().contains(event))
          .flatMap(entry -> guild.getChannelById(entry.getKey()).cast(TextChannel.class));
      });
  }
}
