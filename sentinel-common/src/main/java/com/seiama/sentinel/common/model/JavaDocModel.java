package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import discord4j.common.util.Snowflake;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@NullMarked
public interface JavaDocModel {

  String COLLECTION = "javadocs";

  interface Fields {
    @SuppressWarnings("ConstantName")
    String _ID = AbstractModel._ID;
  }

  interface Partial extends AbstractPartial {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    interface SetUrl extends JavaDocModel.Partial {
      @JsonProperty
      String url();
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    interface SetCommandId extends JavaDocModel.Partial {
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

    public ApplicationCommandRequest asRequest() {
      return ApplicationCommandRequest.builder()
        .name(this.name())
        .description("Search in " + this.name() + " JavaDocs")
        .addOption(ApplicationCommandOptionData.builder()
          .name(REQUEST_OPTION_JAVADOC_KEYWORD)
          .description("The keyword to search for")
          .required(true)
          .type(ApplicationCommandOption.Type.STRING.getValue())
          .autocomplete(true)
          .build())
        .addOption(ApplicationCommandOptionData.builder()
          .name(REQUEST_OPTION_JAVADOC_ELEMENT_TYPE)
          .description("The type of element to search (enum, class, interface, etc)")
          .required(false)
          .type(ApplicationCommandOption.Type.STRING.getValue())
          .autocomplete(true)
          .build())
        .build();
    }
  }

}
