package com.seiama.sentinel.common.discord;

import discord4j.common.util.Snowflake;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class KnownBots {
  /**
   * {@link Snowflake} for {@code Beemo#4570}.
   *
   * @see <a href="https://beemo.gg/">https://beemo.gg/</a>
   */
  public static final Snowflake BEEMO_ID = Snowflake.of("515067662028636170");

  private KnownBots() {
  }
}
