package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.seiama.sentinel.common.annotation.MongoDate;
import discord4j.common.util.Snowflake;
import java.time.Instant;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "modmail")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NullMarked
public class ModMailModel implements AbstractModel {
  private @Id ObjectId _id;
  private Snowflake guild;
  private Instant date;
  private Type type;
  private Snowflake creatorId;
  private String creatorUsername;
  @Deprecated
  private @Nullable Discriminator creatorDiscriminator;
  private Snowflake message;
  private String content;
  private @MongoDate @Nullable Instant threadCreatedAt;
  private @Nullable Snowflake threadCreatedBy;

  public ModMailModel() {
  }

  public ModMailModel(
    final ObjectId _id,
    final Snowflake guild,
    final Instant date,
    final Type type,
    final Snowflake creatorId,
    final String creatorUsername,
    final @Nullable Discriminator creatorDiscriminator,
    final Snowflake message,
    final String content,
    final @Nullable Instant threadCreatedAt,
    final @Nullable Snowflake threadCreatedBy
  ) {
    this._id = _id;
    this.guild = guild;
    this.date = date;
    this.type = type;
    this.creatorId = creatorId;
    this.creatorUsername = creatorUsername;
    this.creatorDiscriminator = creatorDiscriminator;
    this.message = message;
    this.content = content;
    this.threadCreatedAt = threadCreatedAt;
    this.threadCreatedBy = threadCreatedBy;
  }

  @Override
  public ObjectId _id() {
    return this._id;
  }

  public Snowflake guild() {
    return this.guild;
  }

  public Instant date() {
    return this.date;
  }

  public Type type() {
    return this.type;
  }

  public Snowflake creatorId() {
    return this.creatorId;
  }

  public String creatorUsername() {
    return this.creatorUsername;
  }

  public @Nullable Discriminator creatorDiscriminator() {
    return this.creatorDiscriminator;
  }

  public UserIdentity creator() {
    return new UserIdentity(this.creatorId, this.creatorUsername, this.creatorDiscriminator);
  }

  public Snowflake message() {
    return this.message;
  }

  public String content() {
    return this.content;
  }

  public @Nullable Instant threadCreatedAt() {
    return this.threadCreatedAt;
  }

  public void setThreadCreatedAt(final @Nullable Instant threadCreatedAt) {
    this.threadCreatedAt = threadCreatedAt;
  }

  public @Nullable Snowflake threadCreatedBy() {
    return this.threadCreatedBy;
  }

  public void setThreadCreatedBy(final @Nullable Snowflake threadCreatedBy) {
    this.threadCreatedBy = threadCreatedBy;
  }

  public enum Type {
    MODMAIL(new Strings(
      "A new modmail message has been submitted"
    )),
    REPORT(new Strings(
      "A message has been reported"
    ));

    private final Strings strings;

    Type(final Strings strings) {
      this.strings = strings;
    }

    public Strings strings() {
      return this.strings;
    }

    public record Strings(
      String submitted
    ) {
    }
  }
}
