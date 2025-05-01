package com.seiama.sentinel.common.discord;

import discord4j.core.object.entity.Message;
import java.time.Instant;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class Messages {
  private Messages() {
  }

  public static Instant getTimestampOrNow(final Optional<Message> message) {
    return message
      .map(Message::getTimestamp)
      .orElse(Instant.now());
  }
}
