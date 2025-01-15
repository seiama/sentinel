package com.seiama.sentinel.common.discord;

import com.seiama.sentinel.common.model.Discriminator;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import discord4j.core.util.MentionUtil;
import org.jetbrains.annotations.Nullable;

public interface Mention {
  static String userWithId(final User user) {
    return userWithId(user.getId(), user.getUsername(), user.getDiscriminator());
  }

  static String userWithId(final Snowflake id, final @Nullable String username, final @Nullable String discriminator) {
    return userWithId(id, username, new Discriminator(discriminator));
  }

  static String userWithId(final Snowflake id, final @Nullable String username, final @Nullable Discriminator discriminator) {
    final StringBuilder sb = new StringBuilder();
    sb.append(MentionUtil.forUser(id));
    sb.append(" (`");
    if (username != null) {
      if (discriminator == null || discriminator.migrated()) {
        sb.append('@');
      }
      sb.append(username);
      if (discriminator != null && !discriminator.migrated()) {
        sb.append("#").append(discriminator.value());
      }
      sb.append("` / `");
    }
    sb.append(id.asString()).append("`)");
    return sb.toString();
  }
}
