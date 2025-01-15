package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.seiama.sentinel.common.model.response.Response;
import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@NullMarked
public interface FactoidModel {
  String COLLECTION = "factoids";

  interface Fields {
    @SuppressWarnings("ConstantName")
    String _ID = AbstractModel._ID;
  }

  interface Partial extends AbstractPartial {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    interface SetDescriptionAndResponse extends Partial {
      @JsonInclude(JsonInclude.Include.NON_NULL)
      @JsonProperty @Nullable String description();

      @JsonInclude(JsonInclude.Include.NON_NULL)
      @JsonProperty @Nullable Response response();
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    interface SetCommandId extends Partial {
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
    String description,
    Response response,
    @Nullable Snowflake commandId
  ) implements AbstractModel {
    public ApplicationCommandRequest asRequest() {
      return ApplicationCommandRequest.builder()
        .name(this.name())
        .description(this.description())
        .build();
    }
  }
}
