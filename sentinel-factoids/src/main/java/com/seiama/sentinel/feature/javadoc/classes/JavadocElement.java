package com.seiama.sentinel.feature.javadoc.classes;

import com.seiama.sentinel.common.model.JavadocModel;
import com.seiama.sentinel.feature.javadoc.utils.JavaDocUtils;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.component.Container;
import discord4j.core.object.component.Separator;
import discord4j.core.object.component.TextDisplay;
import discord4j.core.object.component.TopLevelMessageComponent;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.rest.util.Color;
import java.util.ArrayList;
import java.util.List;
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

  public JavadocElement(final JavadocItemPartial partial) {
    this.partial = partial;

    final Document document = JavaDocUtils.fetchDocument(this.partial.url());

    if (this.partial.type() == JavadocModel.Complete.ComponentType.PACKAGE) {
      this.elementType = JavadocElementType.PACKAGE;
      this.description = this.readDescription(document.selectFirst("#package-description"));
      this.deprecation = this.readDeprecation(document.selectFirst("#package-description"));
    } else if (this.partial.type() == JavadocModel.Complete.ComponentType.TYPE) {
      this.elementType = JavadocElementType.CLASS;
      this.description = this.readDescription(document.selectFirst("#class-description"));
      this.deprecation = this.readDeprecation(document.selectFirst("#class-description"));
      this.modifiers = this.readModifiers(document);
      final Element headerClassElement = document.selectFirst("div.header > h1.title");
      if (headerClassElement != null) {
        final String headerClassTitle = headerClassElement.attr("title");
        if (headerClassTitle.contains("Exception")) {
          this.elementType = JavadocElementType.EXCEPTION_CLASS;
        } else {
          final String classType = headerClassTitle.replaceAll(" .*", "").toLowerCase(Locale.ROOT);
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

  private void processDetailElements(final Document document, final ClassDetailType detailType, final Consumer<Element> callback) {
    final String detailId = detailType.detailId;

    // Get main blocks to determine what details are available (field, constructor (constr), method)
    final Element detailsSection = document.getElementById(detailId);
    if (detailsSection != null) {
      for (final Element element : detailsSection.select("ul.member-list > li > section.detail")) {
        callback.accept(element);
      }
    }
  }

  @Nullable
  private String readDeprecation(final @Nullable Element element) {
    if (element == null) {
      return null;
    }
    final Element deprecationElement = element.selectFirst("div.deprecation-block");
    if (deprecationElement != null) {
      String deprecationMessage = "";
      final Element deprecationLabelElement = deprecationElement.selectFirst("span.deprecated-label");
      if (deprecationLabelElement != null) {
        deprecationMessage = deprecationMessage.concat(JavaDocUtils.formatText(deprecationLabelElement.attr("style", "font-weight:bold").outerHtml(), this.partial.url())).concat("\n");
      }
      final Element deprecationBlockElement = deprecationElement.selectFirst("div.deprecation-comment");
      if (deprecationBlockElement != null) {
        deprecationMessage = deprecationMessage.concat(JavaDocUtils.formatText(deprecationBlockElement, this.partial.url()));
      } else {
        deprecationMessage = deprecationMessage.concat("not deprecation message was set");
      }
      return deprecationMessage;
    }
    return null;
  }

  @Nullable
  private String readDescription(final @Nullable Element element) {
    if (element == null) {
      return null;
    }
    final Elements elementsDescription = element.select("div.block");
    if (!elementsDescription.isEmpty()) {
      return JavaDocUtils.formatText(elementsDescription.stream().map(Element::outerHtml).collect(Collectors.joining("\n")), this.partial.url());
    }
    return null;
  }

  @Nullable
  private String readModifiers(final @Nullable Element element) {
    if (element == null) {
      return null;
    }
    final Element elementModifiers = element.selectFirst("div[class$=\"-signature\"] > span.modifiers");
    if (elementModifiers != null) {
      // we remove redundant modifiers and clean the response
      return elementModifiers.text().replaceAll("\\b(?!public|private|static|final|protected)\\w+\\b|[@#%&*]", "").trim();
    }
    return null;
  }

  @Nullable
  private String readMethodReturnType(final Element element) {
    final Element elementReturnType = element.selectFirst("div.member-signature > span.return-type");
    if (elementReturnType != null) {
      return JavaDocUtils.formatText(elementReturnType, this.partial.url());
    }
    return null;
  }

  public InteractionApplicationCommandCallbackSpec buildInteractionResponse() {
    final InteractionApplicationCommandCallbackSpec.Builder interactionResponseBuilder = InteractionApplicationCommandCallbackSpec.builder();

    final StringBuilder elementDetails = new StringBuilder();

    if (this.elementType != JavadocElementType.PACKAGE) {
      elementDetails.append("**Package:** ").append(this.partial.packageName()).append("\n");
    }

    elementDetails.append("**Type:** ").append(this.elementType.displayName).append("\n");

    if (this.modifiers != null && !this.modifiers.isBlank()) {
      elementDetails.append("**Modifiers:** ").append(this.modifiers).append("\n");
    }

    if (this.returnType != null) {
      elementDetails.append("**Return:** ").append(this.returnType).append("\n");
    }

    final int elementDetailsLastLine = elementDetails.lastIndexOf("\n");
    if (elementDetailsLastLine >= 0) {
      elementDetails.delete(elementDetailsLastLine, elementDetails.length());
    }

    final List<TopLevelMessageComponent> topComponents = new ArrayList<>();

    final TextDisplay titleDisplayComponent = TextDisplay.of("## " + this.partial.displayTitle());
    final TextDisplay detailsDisplayComponent = TextDisplay.of(elementDetails.toString());

    if (this.description != null) {
      topComponents.add(Container.of(Color.CYAN, titleDisplayComponent, Separator.of(true), TextDisplay.of(this.description), Separator.of(true, Separator.SpacingSize.LARGE), detailsDisplayComponent));
    } else {
      topComponents.add(Container.of(Color.CYAN, titleDisplayComponent, Separator.of(true, Separator.SpacingSize.LARGE), detailsDisplayComponent));
    }

    if (this.deprecation != null) {
      topComponents.add(Container.of(Color.RED, TextDisplay.of(this.deprecation)));
    }

    topComponents.add(ActionRow.of(Button.link(this.partial.url(), "Go to docs")));
    interactionResponseBuilder.components(topComponents);
    return interactionResponseBuilder.build();
  }
}
