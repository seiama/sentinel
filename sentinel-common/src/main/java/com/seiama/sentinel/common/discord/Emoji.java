package com.seiama.sentinel.common.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.object.reaction.ReactionEmoji;
import org.jetbrains.annotations.Nullable;

public interface Emoji {
  ReactionEmoji.Unicode CLOCK1 = ReactionEmoji.unicode("🕐");
  ReactionEmoji.Unicode HAMMER = ReactionEmoji.unicode("🔨");
  ReactionEmoji.Unicode PENCIL = ReactionEmoji.unicode("📝");
  ReactionEmoji.Unicode PERSON_SHRUGGING = ReactionEmoji.unicode("🤷");
  ReactionEmoji.Unicode TADA = ReactionEmoji.unicode("🎉");
  ReactionEmoji.Unicode WARNING = ReactionEmoji.unicode("⚠️");

  ReactionEmoji.Custom NO = ReactionEmoji.custom(Snowflake.of(1093063325899833404L), "no", false);
  ReactionEmoji.Custom UNKNOWN = ReactionEmoji.custom(Snowflake.of(1093063326948401222L), "unknown", false);
  ReactionEmoji.Custom YES = ReactionEmoji.custom(Snowflake.of(1093063327963426896L), "yes", false);
  ReactionEmoji.Custom DOT_BLUE = ReactionEmoji.custom(Snowflake.of(1093064456734507099L), "dot_blue", false); // 0x3498db
  ReactionEmoji.Custom DOT_GREEN = ReactionEmoji.custom(Snowflake.of(1093063322636656680L), "dot_green", false); // 0x43b581
  ReactionEmoji.Custom DOT_GREY = ReactionEmoji.custom(Snowflake.of(1093063901626765333L), "dot_grey", false); // 0xb5988e
  ReactionEmoji.Custom DOT_ORANGE = ReactionEmoji.custom(Snowflake.of(1093063902675345418L), "dot_orange", false); // 0xfaa61a
  ReactionEmoji.Custom DOT_PURPLE = ReactionEmoji.custom(Snowflake.of(1093063904382435381L), "dot_purple", false); // 0x9b59b6
  ReactionEmoji.Custom DOT_RED = ReactionEmoji.custom(Snowflake.of(1093063324662513664L), "dot_red", false); // 0xf04747

  static ReactionEmoji emoji(final @Nullable Boolean value) {
    if (value == null) return NO;
    return value ? YES : NO;
  }
}
