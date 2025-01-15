package com.seiama.sentinel.common.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.util.List;
import org.jetbrains.annotations.Nullable;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Embed(
  @Nullable String title,
  @Nullable String description,
  @Nullable String url,
  @Nullable Integer color,
  @Nullable Footer footer,
  @Nullable Thumbnail thumbnail,
  @Nullable Author author,
  @Nullable List<Field> fields
) {
  public EmbedCreateSpec asCreateSpec() {
    final EmbedCreateSpec.Builder builder = EmbedCreateSpec.builder();
    if (this.title != null) {
      builder.title(this.title);
    }
    if (this.description != null) {
      builder.description(this.description);
    }
    if (this.url != null) {
      builder.url(this.url);
    }
    if (this.color != null) {
      builder.color(Color.of(this.color));
    }
    if (this.footer != null) {
      builder.footer(this.footer.unwrap());
    }
    if (this.thumbnail != null) {
      builder.thumbnail(this.thumbnail.url());
    }
    if (this.author != null) {
      builder.author(this.author.unwrap());
    }
    if (this.fields != null) {
      for (final Field field : this.fields) {
        builder.addField(field.unwrap());
      }
    }
    return builder.build();
  }

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public record Author(
    String name,
    @Nullable String url,
    @Nullable String iconUrl
  ) {
    EmbedCreateFields.Author unwrap() {
      return EmbedCreateFields.Author.of(this.name, this.url, this.iconUrl);
    }
  }

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public record Field(
    String name,
    String value,
    boolean inline
  ) {
    EmbedCreateFields.Field unwrap() {
      return EmbedCreateFields.Field.of(this.name, this.value, Boolean.TRUE.equals(this.inline));
    }
  }

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public record Footer(
    String text,
    @Nullable String iconUrl
  ) {
    EmbedCreateFields.Footer unwrap() {
      return EmbedCreateFields.Footer.of(this.text, this.iconUrl);
    }
  }

  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public record Thumbnail(
    String url
  ) {
  }
}
