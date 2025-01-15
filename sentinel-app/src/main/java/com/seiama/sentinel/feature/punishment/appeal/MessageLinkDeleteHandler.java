package com.seiama.sentinel.feature.punishment.appeal;

import com.seiama.sentinel.model.TemporaryMessageLinkRepository;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.rest.RestClient;
import java.util.function.Function;
import org.reactivestreams.Publisher;

class MessageLinkDeleteHandler implements Function<MessageDeleteEvent, Publisher<Object>> {
  private final TemporaryMessageLinkRepository messageLinks;
  private final RestClient relayRest;

  MessageLinkDeleteHandler(final TemporaryMessageLinkRepository messageLinks, final RestClient relayRest) {
    this.messageLinks = messageLinks;
    this.relayRest = relayRest;
  }

  @Override
  public Publisher<Object> apply(final MessageDeleteEvent event) {
    return this.messageLinks.findBySourceMessageId(event.getMessageId().asLong())
      .flatMap(targetIds -> {
        return this.relayRest.getChannelService().deleteMessage(targetIds.targetChannelId().asLong(), targetIds.targetMessageId().asLong(), null);
      });
  }
}
