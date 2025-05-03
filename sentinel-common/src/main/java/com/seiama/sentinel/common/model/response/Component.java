package com.seiama.sentinel.common.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import discord4j.discordjson.Id;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.MediaGalleryItemData;
import discord4j.discordjson.json.UnfurledMediaItemData;
import discord4j.discordjson.possible.Possible;
import java.util.List;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NullMarked
public record Component(
  @Nullable Integer id,
  int type,
  @Nullable List<Component> components,
  @Nullable Component accessory,
  @Nullable Integer style,
  @Nullable String label,
  @Nullable Emoji emoji,
  @JsonProperty("custom_id")
  @Nullable String customId,
  @Nullable String url,
  @JsonProperty("sku_id")
  @Nullable Id skuId,
  @Nullable Integer spacing,
  @Nullable Boolean divider,
  @Nullable Boolean spoiler,
  @JsonProperty("accent_color")
  @Nullable Integer accentColor,
  @Nullable String description,
  @Nullable String content,
  @Nullable UnfurledMediaItem file,
  @Nullable UnfurledMediaItem media,
  @Nullable List<MediaGalleryItem> items
) {
  public ComponentData unwrap() {
    return ComponentData.builder()
      .id(Possible.ofNullable(this.id))
      .type(this.type)
      .components(Possible.ofNullable(this.components).map(list -> list.stream().map(Component::unwrap).toList()))
      .accessory(Possible.ofNullable(this.accessory).map(Component::unwrap))
      .style(Possible.ofNullable(this.style))
      .label(Possible.ofNullable(this.label))
      .emoji(Possible.ofNullable(this.emoji).map(emojiComponent -> emojiComponent.unwrap().asEmojiData()))
      .customId(Possible.ofNullable(this.customId))
      .url(Possible.ofNullable(this.url))
      .skuId(Possible.ofNullable(this.skuId))
      .spacing(Possible.ofNullable(this.spacing))
      .divider(Possible.ofNullable(this.divider))
      .spoiler(Possible.ofNullable(this.spoiler))
      .accentColor(Possible.ofNullable(Optional.ofNullable(this.accentColor)))
      .description(Possible.ofNullable(Optional.ofNullable(this.description)))
      .content(Possible.ofNullable(this.content))
      .file(Possible.ofNullable(this.file).map(UnfurledMediaItem::unwrap))
      .media(Possible.ofNullable(this.media).map(UnfurledMediaItem::unwrap))
      .items(Possible.ofNullable(this.items).map(list -> list.stream().map(MediaGalleryItem::unwrap).toList()))
      .build();
  }

  @NullMarked
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public record UnfurledMediaItem(
    String url
  ) {
    public UnfurledMediaItemData unwrap() {
      return UnfurledMediaItemData.builder()
        .url(this.url)
        .build();
    }

  }

  @NullMarked
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  public record MediaGalleryItem(
    UnfurledMediaItem media,
    @Nullable String description,
    @Nullable Boolean spoiler
  ) {
    public MediaGalleryItemData unwrap() {
      return MediaGalleryItemData.builder()
        .media(this.media.unwrap())
        .description(Possible.ofNullable(Optional.ofNullable(this.description)))
        .spoiler(Possible.ofNullable(this.spoiler))
        .build();
    }

  }
}
