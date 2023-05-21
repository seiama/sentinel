package com.seiama.sentinel.common.discord;

import com.seiama.sentinel.common.model.Discriminator;
import discord4j.common.util.Snowflake;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MentionTest {
  @Test
  void testUserWithId() {
    assertEquals("<@105923848263753728> (`creamfilled.` / `105923848263753728`)", Mention.userWithId(Snowflake.of("105923848263753728"), "creamfilled.", new Discriminator(Discriminator.TEMPORARY_MIGRATION_MARKER)));
    assertEquals("<@177150983258767360> (`EterNity#0001` / `177150983258767360`)", Mention.userWithId(Snowflake.of("177150983258767360"), "EterNity", "0001"));
  }
}
