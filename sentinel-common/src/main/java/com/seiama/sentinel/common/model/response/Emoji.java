package com.seiama.sentinel.common.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import discord4j.common.util.Snowflake;
import discord4j.core.object.emoji.CustomEmoji;
import discord4j.core.object.emoji.UnicodeEmoji;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NullMarked
public record Emoji(
  String name,
  @Nullable Snowflake id,
  @Nullable Boolean animated
) {
  public discord4j.core.object.emoji.Emoji unwrap() {
    if (this.id != null) {
      return CustomEmoji.of(this.id, this.name, Boolean.TRUE.equals(this.animated));
    } else {
      return UnicodeEmoji.of(this.name);
    }
  }
}
