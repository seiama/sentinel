package com.seiama.sentinel.common.discord;

import com.seiama.sentinel.common.model.Discriminator;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import org.jetbrains.annotations.Nullable;

public interface Mention {
  static String userWithId(final Snowflake id, final @Nullable String username, final @Nullable Discriminator discriminator) {
    return userWithId(id, username, Discriminator.unbox(discriminator));
  }

  static String channelLink(final Snowflake guild, final Snowflake channel) {
    return "https://discord.com/channels/%s/%s".formatted(
      guild.asString(),
      channel.asString()
    );
  }

  static String messageLink(final Guild guild, final Message message) {
    return "https://discord.com/channels/%s/%s/%s".formatted(
      message.getGuildId().orElse(guild.getId()).asString(),
      message.getChannelId().asString(),
      message.getId().asString()
    );
  }

  static String userWithId(final Snowflake id, final @Nullable String username, final @Nullable String discriminator) {
    final StringBuilder sb = new StringBuilder();
    sb.append("<@").append(id.asLong()).append("> (");
    if (username != null) {
      sb.append("`").append(username);
      if (discriminator != null) {
        sb.append("#").append(discriminator);
      }
      sb.append("` / ");
    }
    sb.append("`").append(id.asLong()).append("`)");
    return sb.toString();
  }
}
