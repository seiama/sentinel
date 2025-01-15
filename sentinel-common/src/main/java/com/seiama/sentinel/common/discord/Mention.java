package com.seiama.sentinel.common.discord;

import com.seiama.sentinel.common.model.Discriminator;
import discord4j.common.util.Snowflake;
import discord4j.core.util.MentionUtil;
import org.jetbrains.annotations.Nullable;

public interface Mention {
  static String userWithId(final Snowflake id, final @Nullable String username, final @Nullable Discriminator discriminator) {
    return userWithId(id, username, Discriminator.unbox(discriminator));
  }

  static String userWithId(final Snowflake id, final @Nullable String username, final @Nullable String discriminator) {
    final StringBuilder sb = new StringBuilder();
    sb.append(MentionUtil.forUser(id));
    sb.append(" (`");
    if (username != null) {
      sb.append(username);
      if (discriminator != null) {
        sb.append("#").append(discriminator);
      }
      sb.append("` / `");
    }
    sb.append(id.asString()).append("`)");
    return sb.toString();
  }
}
