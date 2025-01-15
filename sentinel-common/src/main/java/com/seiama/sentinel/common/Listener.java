package com.seiama.sentinel.common;

import discord4j.core.GatewayDiscordClient;
import org.jspecify.annotations.NullMarked;
import reactor.core.publisher.Mono;

@NullMarked
public interface Listener {
  default void connected(final GatewayDiscordClient client) {
  }

  default Mono<Void> listen(final GatewayDiscordClient client) {
    return Mono.empty();
  }
}
