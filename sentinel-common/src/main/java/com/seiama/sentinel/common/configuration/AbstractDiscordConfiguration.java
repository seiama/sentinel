package com.seiama.sentinel.common.configuration;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.presence.ClientPresence;
import discord4j.core.shard.GatewayBootstrap;
import discord4j.core.shard.ShardingStrategy;
import discord4j.gateway.GatewayOptions;
import discord4j.gateway.ShardInfo;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import discord4j.rest.util.AllowedMentions;
import java.util.function.Consumer;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@NullMarked
public abstract class AbstractDiscordConfiguration {
  private static final String DISCORD_TOKEN = "DISCORD_TOKEN";

  @Bean
  GatewayDiscordClient client() {
    return this.client(System.getenv(DISCORD_TOKEN), bootstrap -> {
      bootstrap.setEnabledIntents(this.intents());
      bootstrap.setInitialPresence(this::presence);
    });
  }

  protected abstract IntentSet intents();

  protected abstract ClientPresence presence(final ShardInfo shard);

  @Bean
  @Primary
  RestClient restClient(final GatewayDiscordClient client) {
    return client.getRestClient();
  }

  @Bean("applicationId")
  long applicationId(final RestClient rest) {
    return rest.getApplicationId().blockOptional().orElseThrow();
  }

  @Bean
  ApplicationService applicationService(final RestClient rest) {
    return rest.getApplicationService();
  }

  private GatewayDiscordClient client(final String token, final Consumer<GatewayBootstrap<GatewayOptions>> gateway) {
    final GatewayBootstrap<GatewayOptions> bootstrap = DiscordClientBuilder.create(token)
      .setDefaultAllowedMentions(AllowedMentions.suppressEveryone())
      .build()
      .gateway()
      .setSharding(ShardingStrategy.recommended());
    gateway.accept(bootstrap);
    return bootstrap.login().block();
  }
}
