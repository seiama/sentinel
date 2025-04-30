package com.seiama.sentinel.common.model;

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

@Document(collection = "factoids")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NullMarked
public class FactoidModel implements AbstractModel {
  @Field(_ID)
  @JsonProperty(_ID)
  @Id
  private ObjectId _id;
  private Snowflake guild;
  private String name;
  private String description;
  private Response response;
  private @Nullable Snowflake commandId;

  public FactoidModel() {
  }

  public FactoidModel(
    final ObjectId _id,
    final Snowflake guild,
    final String name,
    final String description,
    final Response response,
    final @Nullable Snowflake commandId
  ) {
    this._id = _id;
    this.guild = guild;
    this.name = name;
    this.description = description;
    this.response = response;
    this.commandId = commandId;
  }

  @Override
  public ObjectId _id() {
    return this._id;
  }

  public Snowflake guild() {
    return this.guild;
  }

  public String name() {
    return this.name;
  }

  public String description() {
    return this.description;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public Response response() {
    return this.response;
  }

  public void setResponse(final Response response) {
    this.response = response;
  }

  public @Nullable Snowflake commandId() {
    return this.commandId;
  }

  public void setCommandId(final @Nullable Snowflake commandId) {
    this.commandId = commandId;
  }

  public ApplicationCommandRequest asRequest() {
    return ApplicationCommandRequest.builder()
      .name(this.name)
      .description(this.description)
      .build();
  }
}
