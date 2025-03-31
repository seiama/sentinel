package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import discord4j.common.util.Snowflake;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@NullMarked
public interface JavadocModel {

  String COLLECTION = "javadocs";

  interface Fields {
    @SuppressWarnings("ConstantName")
    String _ID = AbstractModel._ID;
  }

  interface Partial extends AbstractPartial {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    interface SetUrl extends JavadocModel.Partial {
      @JsonProperty
      String url();
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    interface SetCommandId extends JavadocModel.Partial {
      @JsonProperty
      Snowflake commandId();
    }
  }

  @Document(collection = COLLECTION)
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  record Complete(
    @Field(Fields._ID)
    @JsonProperty(Fields._ID)
    @Id ObjectId _id,
    Snowflake guild,
    String name,
    String url,
    @Nullable Snowflake commandId
  ) implements AbstractModel {

    public static final String REQUEST_OPTION_JAVADOC_KEYWORD = "javadoc-keyword";
    public static final String REQUEST_OPTION_JAVADOC_ELEMENT_TYPE = "javadoc-element-type";

    public String commandName() {
      return "javadoc-".concat(this.name());
    }

    public String commandDescription() {
      return "Search in %s JavaDocs [%s]".formatted(this.name(), this.url());
    }

    private ApplicationCommandOptionData searchOptionRequest() {
      return ApplicationCommandOptionData.builder()
        .name(REQUEST_OPTION_JAVADOC_KEYWORD)
        .description("The keyword to search for")
        .required(true)
        .type(ApplicationCommandOption.Type.STRING.getValue())
        .autocomplete(true)
        .build();
    }

    public ApplicationCommandRequest asRequest() {
      return ApplicationCommandRequest.builder()
        .name(this.commandName())
        .description(this.commandDescription())
        .addOption(ApplicationCommandOptionData.builder()
          .name("search")
          .description("Search in the whole javadoc")
          .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
          .addOption(this.searchOptionRequest())
          .build()
        )
        .addOption(ApplicationCommandOptionData.builder()
          .name("group-search")
          .description("Search in the whole javadoc by an specific type of element")
          .type(ApplicationCommandOption.Type.SUB_COMMAND.getValue())
          .addOption(ApplicationCommandOptionData.builder()
            .name(REQUEST_OPTION_JAVADOC_ELEMENT_TYPE)
            .description("The type of element to search")
            .required(true)
            .type(ApplicationCommandOption.Type.STRING.getValue())
            .choices(ComponentType.asOptionChoices())
            .build())
          .addOption(this.searchOptionRequest())
          .build())
        .build();
    }

    public enum ComponentType {
      ALL("all"),
      MODULE("module"),
      PACKAGE("package"),
      TYPE("type"),
      MEMBER("member"),
      TAG("tag");

      private final String name;

      ComponentType(final String name) {
        this.name = name;
      }

      public String displayName() {
        return this.name;
      }

      public ApplicationCommandOptionChoiceData asOptionChoice() {
        return ApplicationCommandOptionChoiceData.builder().name(this.name).value(this.toString()).build();
      }

      public static ComponentType fromString(final String name) {
        for (final ComponentType type : values()) {
          if (type.name.equalsIgnoreCase(name)) {
            return type;
          }
        }
        return ALL;
      }

      public static Collection<ApplicationCommandOptionChoiceData> asOptionChoices() {
        return Arrays.stream(values()).filter(componentType -> componentType != ALL).map(ComponentType::asOptionChoice).collect(Collectors.toList());
      }
    }
  }

}
