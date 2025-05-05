package com.seiama.sentinel.common.discord;

import com.seiama.sentinel.common.model.Discriminator;
import com.seiama.sentinel.common.model.UserIdentity;
import discord4j.common.util.Snowflake;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserDisplayTest {
  private static final UserIdentity CREAMFILLED = new UserIdentity(Snowflake.of("105923848263753728"), "creamfilled.", new Discriminator(Discriminator.TEMPORARY_MIGRATION_MARKER));
  private static final UserIdentity NEW_ETERNITY = new UserIdentity(Snowflake.of("177150983258767360"), "eternity._.", new Discriminator(Discriminator.TEMPORARY_MIGRATION_MARKER));
  private static final UserIdentity OLD_ETERNITY = new UserIdentity(Snowflake.of("177150983258767360"), "EterNity", new Discriminator("0001"));

  @Test
  void testUsername() {
    assertEquals("@creamfilled.", UserDisplay.render(UserDisplay.Renderer.USERNAME, CREAMFILLED));
    assertEquals("@eternity._.", UserDisplay.render(UserDisplay.Renderer.USERNAME, NEW_ETERNITY));
    assertEquals("EterNity#0001", UserDisplay.render(UserDisplay.Renderer.USERNAME, OLD_ETERNITY));
  }

  @Test
  void testMentionWithTrailingBacktickWrappedUsernameAndId() {
    assertEquals("<@105923848263753728> (`@creamfilled.` / `105923848263753728`)", UserDisplay.render(UserDisplay.Renderer.MENTION_WITH_TRAILING_BACKTICK_WRAPPED_USERNAME_AND_ID, CREAMFILLED));
    assertEquals("<@177150983258767360> (`@eternity._.` / `177150983258767360`)", UserDisplay.render(UserDisplay.Renderer.MENTION_WITH_TRAILING_BACKTICK_WRAPPED_USERNAME_AND_ID, NEW_ETERNITY));
    assertEquals("<@177150983258767360> (`EterNity#0001` / `177150983258767360`)", UserDisplay.render(UserDisplay.Renderer.MENTION_WITH_TRAILING_BACKTICK_WRAPPED_USERNAME_AND_ID, OLD_ETERNITY));
  }
}
