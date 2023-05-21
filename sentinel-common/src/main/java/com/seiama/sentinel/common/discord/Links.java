package com.seiama.sentinel.common.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;

public interface Links {
  static String channel(final Snowflake guild, final Snowflake channel) {
    return "https://discord.com/channels/%s/%s".formatted(
      guild.asString(),
      channel.asString()
    );
  }

  static String message(final Guild guild, final Message message) {
    return "https://discord.com/channels/%s/%s/%s".formatted(
      message.getGuildId().orElse(guild.getId()).asString(),
      message.getChannelId().asString(),
      message.getId().asString()
    );
  }
}
