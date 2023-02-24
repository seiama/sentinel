package com.seiama.sentinel.core;

import discord4j.core.GatewayDiscordClient;
import org.jetbrains.annotations.NotNull;
import reactor.core.publisher.Mono;

public interface Listener {
  default void connected(final @NotNull GatewayDiscordClient client) {
  }

  default @NotNull Mono<Void> listen(final @NotNull GatewayDiscordClient client) {
    return Mono.empty();
  }
}
