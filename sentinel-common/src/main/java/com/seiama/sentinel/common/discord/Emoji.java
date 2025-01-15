package com.seiama.sentinel.common.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.object.reaction.ReactionEmoji;
import org.jetbrains.annotations.Nullable;

public interface Emoji {
  ReactionEmoji.Unicode CLOCK1 = ReactionEmoji.unicode("🕐");
  ReactionEmoji.Unicode HAMMER = ReactionEmoji.unicode("🔨");
  ReactionEmoji.Unicode PERSON_SHRUGGING = ReactionEmoji.unicode("🤷");
  ReactionEmoji.Unicode TADA = ReactionEmoji.unicode("🎉");
  ReactionEmoji.Unicode WARNING = ReactionEmoji.unicode("⚠️");

  ReactionEmoji.Custom NO = ReactionEmoji.custom(Snowflake.of(1080888011639767091L), "no", false); // ReactionEmoji.custom(Snowflake.of(1078266825630027786L), "no", false);
  ReactionEmoji.Custom UNKNOWN = ReactionEmoji.custom(Snowflake.of(1078266827626512475L), "unknown", false);
  ReactionEmoji.Custom YES = ReactionEmoji.custom(Snowflake.of(1080888013275533372L), "yes", false); // ReactionEmoji.custom(Snowflake.of(1078266829107101716L), "yes", false);
  ReactionEmoji.Custom DOT_GREY = ReactionEmoji.custom(Snowflake.of(1078266818747191317L), "dot_grey", false);
  ReactionEmoji.Custom DOT_ORANGE = ReactionEmoji.custom(Snowflake.of(1078266820584275968L), "dot_orange", false);
  ReactionEmoji.Custom DOT_PURPLE = ReactionEmoji.custom(Snowflake.of(1078266822509461584L), "dot_purple", false);
  ReactionEmoji.Custom DOT_RED = ReactionEmoji.custom(Snowflake.of(978536050941108225L), "punishment_ban", false); // ReactionEmoji.custom(Snowflake.of(1078266824455626752L), "dot_red", false);

  static ReactionEmoji emoji(final @Nullable Boolean value) {
    if (value == null) return NO;
    return value ? YES : NO;
  }

  static String toString(final ReactionEmoji emoji) {
    if (emoji instanceof ReactionEmoji.Unicode unicode) return unicode.getRaw();
    if (emoji instanceof ReactionEmoji.Custom custom) return custom.asFormat();
    throw new IllegalArgumentException(emoji.toString());
  }
}
