package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.seiama.sentinel.common.annotation.MongoDate;
import com.seiama.sentinel.common.jackson.InstantExtendedJsonSerializer;
import discord4j.common.util.Snowflake;
import java.time.Instant;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@NullMarked
public interface ModMailModel {
  String COLLECTION = "modmail";

  interface Fields {
    @SuppressWarnings("ConstantName")
    String _ID = AbstractModel._ID;
  }

  interface Partial extends AbstractPartial {
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @SuppressWarnings("EmptyLineSeparator")
    interface ThreadCreated extends Partial {
      @JsonSerialize(using = InstantExtendedJsonSerializer.class)
      @JsonProperty @MongoDate Instant threadCreatedAt();
      @JsonProperty Snowflake threadCreatedBy();
    }
  }

  @Document(collection = COLLECTION)
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  record Complete(
    @Field(Fields._ID)
    @JsonProperty(Fields._ID)
    @Id ObjectId _id,
    Snowflake guild,
    Instant date,
    Type type,
    Snowflake creatorId,
    String creatorUsername,
    @Deprecated
    @Nullable Discriminator creatorDiscriminator,
    Snowflake message,
    String content,
    @MongoDate @Nullable Instant threadCreatedAt,
    @Nullable Snowflake threadCreatedBy
  ) implements AbstractModel {
    public UserIdentity creator() {
      return new UserIdentity(this.creatorId, this.creatorUsername, this.creatorDiscriminator);
    }
  }

  enum Type {
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
