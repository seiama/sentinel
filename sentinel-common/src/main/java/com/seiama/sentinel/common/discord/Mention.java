package com.seiama.sentinel.common.discord;

import discord4j.common.util.Snowflake;
import org.jetbrains.annotations.Nullable;

public interface Mention {
  static String userWithId(final Snowflake id, final @Nullable String username, final @Nullable String discriminator) {
    final StringBuilder sb = new StringBuilder();
    sb.append("<@").append(id.asLong()).append("> (");
    if (username != null && discriminator != null) {
      sb.append("`").append(username).append("#").append(discriminator).append("` / ");
    }
    sb.append("`").append(id.asLong()).append("`)");
    return sb.toString();
  }
}
