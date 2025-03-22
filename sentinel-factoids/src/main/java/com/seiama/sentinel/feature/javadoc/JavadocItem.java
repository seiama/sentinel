package com.seiama.sentinel.feature.javadoc;

import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.rest.util.Color;

public record JavadocItem(
  String url,
  String type,
  String packagePath,
  String name,
  String description,
  boolean deprecated,
  String deprecatedMessage
) {

  public InteractionApplicationCommandCallbackSpec buildInteractionResponse() {
    InteractionApplicationCommandCallbackSpec.Builder interactionResponseBuilder = InteractionApplicationCommandCallbackSpec.builder();
    if (this.deprecated()) {
      interactionResponseBuilder.content("> This element is deprecated: ```" + (this.deprecatedMessage().isBlank() ? "no deprecation message set." : this.deprecatedMessage()) + "```");
    }
    EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder();
    embedBuilder.color(Color.CINNABAR)
      .title(this.name)
      .description(this.description)
      .addField(EmbedCreateFields.Field.of("Type:", this.type, true))
      .addField(EmbedCreateFields.Field.of("Path:", this.packagePath, true));
    interactionResponseBuilder.addEmbed(embedBuilder.build());
    interactionResponseBuilder.components(ActionRow.of(Button.link(this.url(), "Go to docs")));
    return interactionResponseBuilder.build();
  }

}
