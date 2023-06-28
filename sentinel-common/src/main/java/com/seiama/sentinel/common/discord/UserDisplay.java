package com.seiama.sentinel.common.discord;

import com.seiama.sentinel.common.model.Discriminator;
import com.seiama.sentinel.common.model.UserIdentity;
import discord4j.common.util.Snowflake;
import discord4j.core.util.MentionUtil;
import org.jetbrains.annotations.Nullable;

public interface UserDisplay {
  static String render(final Renderer renderer, final UserIdentity identity) {
    return renderer.render(identity);
  }

  interface Renderer {
    Renderer USERNAME = identity -> {
      final String username = identity.username();
      final @Nullable Discriminator discriminator = identity.discriminator();
      final StringBuilder sb = new StringBuilder();
      if (discriminator != null && discriminator.migrated()) {
        sb.append('@');
      }
      sb.append(username);
      if (discriminator != null && !discriminator.migrated()) {
        sb.append("#").append(discriminator.value());
      }
      return sb.toString();
    };
    Renderer MENTION = identity -> MentionUtil.forUser(identity.id());
    Renderer MENTION_WITH_TRAILING_BACKTICK_WRAPPED_USERNAME_AND_ID = identity -> {
      final Snowflake id = identity.id();
      final String username = identity.username();
      final @Nullable Discriminator discriminator = identity.discriminator();
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
    };

    String render(final UserIdentity identity);
  }
}
