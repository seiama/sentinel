package com.seiama.sentinel.configuration;

import com.seiama.sentinel.common.configuration.AbstractDiscordConfiguration;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.presence.ClientPresence;
import discord4j.gateway.ShardInfo;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.RestClient;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@NullMarked
public class DiscordConfiguration extends AbstractDiscordConfiguration {
  private static final String DISCORD_TOKEN_FACTOIDS = "DISCORD_TOKEN_FACTOIDS";
  private static final String DISCORD_TOKEN_RELAY = "DISCORD_TOKEN_RELAY";

  @Override
  protected IntentSet intents() {
    return IntentSet.of(
      Intent.GUILDS,
      Intent.GUILD_MEMBERS,
      Intent.GUILD_MODERATION,
      Intent.GUILD_MESSAGES,
      Intent.GUILD_MESSAGE_REACTIONS,
      Intent.MESSAGE_CONTENT
    );
  }

  @Override
  protected ClientPresence presence(final ShardInfo shard) {
    return ClientPresence.online();
  }

  @Bean("factoidsRest")
  RestClient factoidsRest() {
    return RestClient.create(System.getenv(DISCORD_TOKEN_FACTOIDS));
  }

  @Bean("relayClient")
  GatewayDiscordClient relayClient() {
    return this.client(System.getenv(DISCORD_TOKEN_RELAY), bootstrap -> {
      bootstrap.setEnabledIntents(this.intents());
      bootstrap.setInitialPresence(this::presence);
    });
  }

  @Bean("relayRest")
  RestClient relayRest() {
    return RestClient.create(System.getenv(DISCORD_TOKEN_RELAY));
  }
}
