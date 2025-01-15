package com.seiama.sentinel.common.model.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.collect.Lists;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.LayoutComponent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackReplyMono;
import discord4j.discordjson.possible.Possible;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record Response(
  @Nullable String content,
  @Nullable List<Embed> embeds,
  @Nullable List<Button> buttons
) {
  public InteractionApplicationCommandCallbackReplyMono decorate(final InteractionApplicationCommandCallbackReplyMono mono) {
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
    if (this.buttons != null) {
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
}
