package com.seiama.sentinel.feature.punishment.appeal;

import com.seiama.sentinel.common.discord.Discord;
import com.seiama.sentinel.common.model.AppealModel;
import com.seiama.sentinel.common.model.AppealRepository;
import com.seiama.sentinel.common.model.Feature;
import com.seiama.sentinel.common.model.GuildModel;
import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.model.TemporaryMessageLink;
import com.seiama.sentinel.model.TemporaryMessageLinkRepository;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;
import discord4j.rest.RestClient;
import java.util.function.Function;
import java.util.function.Supplier;
import org.jspecify.annotations.NullMarked;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

@NullMarked
class MessageLinkCreateHandler implements Function<MessageCreateEvent, Publisher<Object>> {
  private static final Logger LOGGER = LoggerFactory.getLogger(MessageLinkCreateHandler.class);
  private final GuildRepository guilds;
  private final AppealRepository appeals;
  private final TemporaryMessageLinkRepository messageLinks;
  private final RestClient relayRest;

  MessageLinkCreateHandler(final GuildRepository guilds, final AppealRepository appeals, final TemporaryMessageLinkRepository messageLinks, final RestClient relayRest) {
    this.guilds = guilds;
    this.appeals = appeals;
    this.messageLinks = messageLinks;
    this.relayRest = relayRest;
  }

  @Override
  public Publisher<Object> apply(final MessageCreateEvent event) {
    final Message message = event.getMessage();
    if (Discord.isBot(message.getAuthor())) {
      return Mono.empty();
    }
    return message.getGuild()
      .zipWhen(guild -> this.guilds.findByGuildOrFeaturesPunishmentsAppealsGuild(guild.getId(), guild.getId()))
      .filter(tuple -> Feature.APPEALS.enabledForGuild(tuple.getT2()))
      .zipWith(message.getChannel())
      // todo: filter channel by ids
      .flatMap(TupleUtils.function((guild, channel) -> {
        final Channel.Type type = channel.getType();
        if (type == Channel.Type.GUILD_TEXT) {
          return Mono.just(Tuples.<Tuple2<Guild, GuildModel.Complete>, Supplier<Mono<AppealModel.Complete>>, Function<AppealModel.Complete, Snowflake>>of(
            guild,
            () -> this.appeals.findByAppealChannel(channel.getId()),
            AppealModel.Complete::appealThread
          ));
        } else if (type == Channel.Type.GUILD_PRIVATE_THREAD) {
          return Mono.just(Tuples.<Tuple2<Guild, GuildModel.Complete>, Supplier<Mono<AppealModel.Complete>>, Function<AppealModel.Complete, Snowflake>>of(
            guild,
            () -> this.appeals.findByAppealThread(channel.getId()),
            AppealModel.Complete::appealChannel
          ));
        }
        return Mono.empty();
      }))
      .flatMap(TupleUtils.function((guild, model, targetChannelId) -> {
        return model.get()
          .map(targetChannelId)
          .flatMap(channelId -> this.relayRest.getChannelById(channelId).createMessage(Appeals.createMessage(message.getData(), message.getAuthor(), guild)))
          .doOnError(t -> LOGGER.error("Error sending relay message", t))
          .flatMap(newMessage -> this.messageLinks.save(new TemporaryMessageLink(
            guild.getT2()._id(),
            message.getChannelId(),
            message.getId(),
            Snowflake.of(newMessage.channelId()),
            Snowflake.of(newMessage.id())
          )));
      }));
  }
}
