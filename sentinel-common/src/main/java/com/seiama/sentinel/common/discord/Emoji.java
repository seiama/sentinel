package com.seiama.sentinel.common.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.object.emoji.CustomEmoji;
import discord4j.core.object.emoji.UnicodeEmoji;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface Emoji {
  UnicodeEmoji CLOCK1 = UnicodeEmoji.of("🕐");
  UnicodeEmoji HAMMER = UnicodeEmoji.of("🔨");
  UnicodeEmoji PENCIL = UnicodeEmoji.of("📝");
  UnicodeEmoji PERSON_SHRUGGING = UnicodeEmoji.of("🤷");
  UnicodeEmoji TADA = UnicodeEmoji.of("🎉");
  UnicodeEmoji WARNING = UnicodeEmoji.of("⚠️");

  CustomEmoji NO = CustomEmoji.of(Snowflake.of(1093063325899833404L), "no", false);
  CustomEmoji UNKNOWN = CustomEmoji.of(Snowflake.of(1093063326948401222L), "unknown", false);
  CustomEmoji YES = CustomEmoji.of(Snowflake.of(1093063327963426896L), "yes", false);
  CustomEmoji DOT_BLUE = CustomEmoji.of(Snowflake.of(1093064456734507099L), "dot_blue", false); // 0x3498db
  CustomEmoji DOT_GREEN = CustomEmoji.of(Snowflake.of(1093063322636656680L), "dot_green", false); // 0x43b581
  CustomEmoji DOT_GREY = CustomEmoji.of(Snowflake.of(1093063901626765333L), "dot_grey", false); // 0xb5988e
  CustomEmoji DOT_ORANGE = CustomEmoji.of(Snowflake.of(1093063902675345418L), "dot_orange", false); // 0xfaa61a
  CustomEmoji DOT_PURPLE = CustomEmoji.of(Snowflake.of(1093063904382435381L), "dot_purple", false); // 0x9b59b6
  CustomEmoji DOT_RED = CustomEmoji.of(Snowflake.of(1093063324662513664L), "dot_red", false); // 0xf04747

  static discord4j.core.object.emoji.Emoji emoji(final @Nullable Boolean value) {
    if (value == null) return NO;
    return value ? YES : NO;
  }
}
