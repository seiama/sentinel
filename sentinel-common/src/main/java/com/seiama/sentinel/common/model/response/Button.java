package com.seiama.sentinel.common.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NullMarked
public record Button(
  Emoji emoji,
  String label,
  @Nullable String url
) {
  public discord4j.core.object.component.Button unwrap() {
    if (this.url != null) {
      return discord4j.core.object.component.Button.link(
        this.url,
        this.emoji.unwrap(),
        this.label
      );
    }
    throw new IllegalStateException();
  }
}
