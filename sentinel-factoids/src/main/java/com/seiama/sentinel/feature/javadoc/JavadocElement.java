package com.seiama.sentinel.feature.javadoc;

import com.seiama.sentinel.common.model.JavadocModel;
import com.seiama.sentinel.feature.javadoc.utils.JSoupUtils;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.rest.util.Color;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class JavadocElement {
  private final JavadocItemPartial partial;
  private JavadocElementType elementType = JavadocElementType.UNKNOWN;
  @Nullable
  private String deprecation;
  @Nullable
  private String description;
  @Nullable
  private String modifiers;
  @Nullable
  private String returnType;

  public JavadocElement(JavadocItemPartial partial) {
    this.partial = partial;

    Document document = JSoupUtils.fetchDocument(this.partial.url());

    if (this.partial.type() == JavadocModel.Complete.ComponentType.PACKAGE) {
      this.elementType = JavadocElementType.PACKAGE;
      this.description = this.readDescription(document.selectFirst("#package-description"));
      this.deprecation = this.readDeprecation(document.selectFirst("#package-description"));
    } else if (this.partial.type() == JavadocModel.Complete.ComponentType.TYPE) {
      this.elementType = JavadocElementType.CLASS;
      this.description = this.readDescription(document.selectFirst("#class-description"));
      this.deprecation = this.readDeprecation(document.selectFirst("#class-description"));
      this.modifiers = this.readModifiers(document);
      Element headerClassElement = document.selectFirst("div.header > h1.title");
      if (headerClassElement != null) {
        String headerClassTitle = headerClassElement.attr("title");
        if (headerClassTitle.contains("Exception")) {
          this.elementType = JavadocElementType.EXCEPTION_CLASS;
        } else {
          String classType = headerClassTitle.replaceAll(" .*", "").toLowerCase(Locale.ROOT);
          switch (classType) {
            case "interface" -> this.elementType = JavadocElementType.INTERFACE;
            case "enum" -> this.elementType = JavadocElementType.ENUM_CLASS;
            case "record" -> this.elementType = JavadocElementType.RECORD_CLASS;
            case "annotation" -> this.elementType = JavadocElementType.ANNOTATION_INTERFACE;
          }
        }
      }
    } else if (this.partial.type() == JavadocModel.Complete.ComponentType.MEMBER) {
      if (this.partial.url().contains("(")) {
        this.processDetailElements(document, ClassDetailType.METHOD, element -> {
          if (this.partial.urlDecoded().contains(element.id())) {
            this.elementType = JavadocElementType.METHOD;
            this.description = this.readDescription(element);
            this.deprecation = this.readDeprecation(element);
            this.modifiers = this.readModifiers(element);
            this.returnType = this.readMethodReturnType(element);
          }
        });
        this.processDetailElements(document, ClassDetailType.CONSTRUCTOR, element -> {
          if (this.partial.urlDecoded().contains(element.id())) {
            this.elementType = JavadocElementType.CONSTRUCTOR;
            this.description = this.readDescription(element);
            this.deprecation = this.readDeprecation(element);
            this.modifiers = this.readModifiers(element);
          }
        });
      } else {
        // This can be a field or enum constant then need handle all this
        this.processDetailElements(document, ClassDetailType.FIELD, element -> {
          if (element.id().equals(this.partial.name())) {
            this.elementType = JavadocElementType.FIELD;
            this.description = this.readDescription(element);
            this.deprecation = this.readDeprecation(element);
          }
        });
        this.processDetailElements(document, ClassDetailType.ENUM_CONSTANTS, element -> {
          if (element.id().equals(this.partial.name())) {
            this.elementType = JavadocElementType.ENUM_ELEMENT;
            this.description = this.readDescription(element);
            this.deprecation = this.readDeprecation(element);
          }
        });
      }
    }
  }

  private void processDetailElements(Document document, ClassDetailType detailType, Consumer<Element> callback) {
    String detailId = detailType.detailId;

    // Get main blocks to determine what details are available (field, constructor (constr), method)
    Element detailsSection = document.getElementById(detailId);
    if (detailsSection != null) {
      for (Element element : detailsSection.select("ul.member-list > li > section.detail")) {
        callback.accept(element);
      }
    }
  }

  @Nullable
  private String readDeprecation(@Nullable Element element) {
    if (element == null) {
      return null;
    }
    Element deprecationElement = element.selectFirst("div.deprecation-block");
    if (deprecationElement != null) {
        String deprecationMessage = "";
        Element deprecationLabelElement = deprecationElement.selectFirst("span.deprecated-label");
        if (deprecationLabelElement != null) {
          deprecationMessage = deprecationMessage.concat(JSoupUtils.formatText(deprecationLabelElement.attr("style", "font-weight:bold").outerHtml(), this.partial.url())).concat("\n");
        }
        Element deprecationBlockElement = deprecationElement.selectFirst("div.deprecation-comment");
        if (deprecationBlockElement != null) {
          deprecationMessage = deprecationMessage.concat(JSoupUtils.formatText(deprecationBlockElement, this.partial.url()));
        } else {
          deprecationMessage = deprecationMessage.concat("not deprecation message was set");
        }
        return deprecationMessage;
    }
    return null;
  }

  @Nullable
  private String readDescription(@Nullable Element element) {
    if (element == null) {
      return null;
    }
    Elements elementsDescription = element.select("div.block");
    if (!elementsDescription.isEmpty()) {
      return description = JSoupUtils.formatText(elementsDescription.stream().map(Element::outerHtml).collect(Collectors.joining("\n")), this.partial.url());
    }
    return null;
  }

  @Nullable
  private String readModifiers(@Nullable Element element) {
    if (element == null) {
      return null;
    }
    Element elementModifiers = element.selectFirst("div[class$=\"-signature\"] > span.modifiers");
    if (elementModifiers != null) {
      return elementModifiers.text().replaceAll("\\b(?!public|private|static|final|protected)\\w+\\b|[@#%&*]", "").trim();
    }
    return null;
  }

  @Nullable
  private String readMethodReturnType(Element element) {
    Element elementReturnType = element.selectFirst("div.member-signature > span.return-type");
    if (elementReturnType != null) {
      return JSoupUtils.formatText(elementReturnType, this.partial.url());
    }
    return null;
  }

  public InteractionApplicationCommandCallbackSpec buildInteractionResponse() {
    InteractionApplicationCommandCallbackSpec.Builder interactionResponseBuilder = InteractionApplicationCommandCallbackSpec.builder();

    if (this.deprecation != null) {
      EmbedCreateSpec.Builder embedDeprecatedMessageBuilder = EmbedCreateSpec.builder();
      embedDeprecatedMessageBuilder.color(Color.RED);
      embedDeprecatedMessageBuilder.description(this.deprecation);
      interactionResponseBuilder.addEmbed(embedDeprecatedMessageBuilder.build());
    }

    EmbedCreateSpec.Builder embedBuilder = EmbedCreateSpec.builder();
    embedBuilder.color(Color.CYAN).title(this.partial.displayTitle());

    if (this.description != null) {
      embedBuilder.description(this.description);
    }

    if (this.elementType != JavadocElementType.PACKAGE) {
      embedBuilder.addField(EmbedCreateFields.Field.of("Package:", this.partial.packageName(), true));
    }

    embedBuilder.addField(EmbedCreateFields.Field.of("Type:", this.elementType.displayName, true));

    if (this.modifiers != null) {
      embedBuilder.addField(EmbedCreateFields.Field.of("Modifiers:", this.modifiers, true));
    }

    if (this.returnType != null) {
      embedBuilder.addField(EmbedCreateFields.Field.of("Return:", this.returnType, true));
    }

    interactionResponseBuilder.addEmbed(embedBuilder.build());
    interactionResponseBuilder.components(ActionRow.of(Button.link(this.partial.url(), "Go to docs")));
    return interactionResponseBuilder.build();
  }
}
