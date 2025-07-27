package com.seiama.sentinel.feature.punishment.appeal;

import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.model.TemporaryMessageLinkRepository;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.discordjson.json.MessageEditRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.RestClient;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.jspecify.annotations.NullMarked;
import org.reactivestreams.Publisher;
import reactor.function.TupleUtils;
import reactor.util.function.Tuples;

@NullMarked
class MessageLinkUpdateHandler implements Function<MessageUpdateEvent, Publisher<Object>> {
  private final GuildRepository guilds;
  private final TemporaryMessageLinkRepository messageLinks;
  private final RestClient relayRest;

  MessageLinkUpdateHandler(final GuildRepository guilds, final TemporaryMessageLinkRepository messageLinks, final RestClient relayRest) {
    this.guilds = guilds;
    this.messageLinks = messageLinks;
    this.relayRest = relayRest;
  }

  @Override
  public Publisher<Object> apply(final MessageUpdateEvent event) {
    return this.messageLinks.findBySourceMessageId(event.getMessageId().asLong())
      .flatMap(targetIds -> {
        return event.getGuild()
          .zipWith(event.getMessage())
          .flatMap(TupleUtils.function((guild, message) -> {
            return this.guilds.findById(targetIds.guild())
              .flatMap(guildModel -> {
                return this.relayRest.getMessageById(targetIds.targetChannelId(), targetIds.targetMessageId()).edit(
                  MessageEditRequest.builder()
                    .embeds(Possible.of(Optional.of(List.of(
                      Appeals.createMessage(message.getData(), message.getAuthor(), Tuples.of(guild, guildModel)).asRequest()
                    ))))
                    .build()
                );
              });
          }));
      });
  }
}
