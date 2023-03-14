package com.seiama.sentinel.configuration;

import com.seiama.sentinel.common.configuration.AbstractDiscordConfiguration;
import discord4j.core.object.presence.ClientPresence;
import discord4j.gateway.ShardInfo;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordConfiguration extends AbstractDiscordConfiguration {
  @Override
  protected IntentSet intents() {
    return IntentSet.of(
      Intent.GUILDS
    );
  }

  @Override
  protected ClientPresence presence(final ShardInfo shard) {
    return ClientPresence.online();
  }
}
