package com.seiama.sentinel.common.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import discord4j.common.util.Snowflake;
import discord4j.core.object.reaction.ReactionEmoji;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NullMarked
public record Emoji(
  String name,
  @Nullable Snowflake id,
  boolean animated
) {
  public ReactionEmoji unwrap() {
    if (this.id != null) {
      return ReactionEmoji.custom(this.id, this.name, this.animated);
    } else {
      return ReactionEmoji.unicode(this.name);
    }
  }
}
