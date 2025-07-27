package com.seiama.sentinel.feature.punishment.appeal;

import com.seiama.sentinel.model.TemporaryMessageLinkRepository;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.object.entity.Message;
import java.util.function.Function;
import org.jspecify.annotations.NullMarked;
import org.reactivestreams.Publisher;

@NullMarked
class MessageLinkDeleteHandler implements Function<MessageDeleteEvent, Publisher<Object>> {
  private final TemporaryMessageLinkRepository messageLinks;
  private final GatewayDiscordClient relayRest;

  MessageLinkDeleteHandler(final TemporaryMessageLinkRepository messageLinks, final GatewayDiscordClient relayRest) {
    this.messageLinks = messageLinks;
    this.relayRest = relayRest;
  }

  @Override
  public Publisher<Object> apply(final MessageDeleteEvent event) {
    return this.messageLinks.findBySourceMessageId(event.getMessageId().asLong())
      .flatMap(targetIds -> {
        return this.relayRest.getMessageById(targetIds.targetChannelId(), targetIds.targetMessageId()).flatMap(Message::delete);
      });
  }
}
