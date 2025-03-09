package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import discord4j.common.util.Snowflake;
import discord4j.discordjson.json.ApplicationCommandRequest;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
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
  }

  @Document(collection = COLLECTION)
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  record Complete(
    @Field(Fields._ID)
    @JsonProperty(Fields._ID)
    @Id ObjectId _id,
    Snowflake guild,
    String name,
    String url
  ) implements AbstractModel {
    public ApplicationCommandRequest asRequest() {
      return ApplicationCommandRequest.builder()
        .name(this.name())
        .build();
    }
  }

}
