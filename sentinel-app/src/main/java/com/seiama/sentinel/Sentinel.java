package com.seiama.sentinel;

import com.seiama.sentinel.common.Listener;
import discord4j.core.GatewayDiscordClient;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;

@SpringBootApplication
public class Sentinel {
  public static final Instant BOOT_TIME = Instant.now();

  public static void main(final String[] args) {
    SpringApplication.run(Sentinel.class, args);
  }

  private final GatewayDiscordClient client;
  private final Set<Listener> listeners;

  @Autowired
  public Sentinel(final GatewayDiscordClient client, final Set<Listener> listeners) {
    this.client = client;
    this.listeners = listeners;
  }

  @PostConstruct
  public void boot() {
    Flux.fromIterable(this.listeners)
      .doOnNext(listener -> listener.connected(this.client))
      .flatMap(listener -> listener.listen(this.client))
      .then(this.client.onDisconnect())
      .subscribe();
  }
}
