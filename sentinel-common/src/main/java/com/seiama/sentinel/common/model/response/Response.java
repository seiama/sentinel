package com.seiama.sentinel.common.model.response;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.collect.Lists;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.object.component.MessageComponent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import discord4j.discordjson.json.ComponentData;
import discord4j.discordjson.json.ImmutableComponentData;
import discord4j.discordjson.possible.Possible;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NullMarked
public record Response(
  @Nullable String content,
  @JsonDeserialize(builder = ImmutableComponentData.Builder.class)
  @Nullable List<ComponentData> components,
  @Nullable List<Embed> embeds,
  @Nullable List<Button> buttons
) {
  public InteractionApplicationCommandCallbackReplyMono decorate(final InteractionApplicationCommandCallbackReplyMono mono) {
    if (this.useComponentsV2()) {
      return mono.withComponents(this.wrapComponents());
    }
    return mono
      .withContent(this.wrapContent())
      .withEmbeds(this.wrapEmbeds())
      .withComponents(this.wrapComponents());
  }

  public Possible<String> wrapContent() {
    return this.content != null
      ? Possible.of(this.content)
      : Possible.absent();
  }

  public Possible<List<EmbedCreateSpec>> wrapEmbeds() {
    return (this.embeds != null && !this.embeds.isEmpty())
      ? Possible.of(this.embeds.stream().map(Embed::asCreateSpec).toList())
      : Possible.absent();
  }

  public Possible<List<LayoutComponent>> wrapComponents() {
    if (this.components != null && !this.components.isEmpty()) {
      return Possible.of(this.components.stream().map(MessageComponent::fromData).filter(messageComponent -> messageComponent instanceof LayoutComponent).map(LayoutComponent.class::cast).toList());
    } else if (this.buttons != null) {
      final List<LayoutComponent> components = new ArrayList<>();
      for (final List<Button> buttons : Lists.partition(this.buttons, 5)) {
        components.add(ActionRow.of(
          buttons.stream()
            .map(Button::unwrap)
            .toList()
        ));
      }
      return Possible.of(components);
    }
    return Possible.absent();
  }

  private boolean useComponentsV2() {
    return this.components != null && this.components.stream().anyMatch(componentData -> MessageComponent.Type.of(componentData.type()).isRequiredFlag());
  }
}
