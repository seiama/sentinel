package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import discord4j.common.util.Snowflake;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

public interface GuildModel {
  String COLLECTION = "guilds";

  interface Fields {
    @SuppressWarnings("ConstantName")
    String _ID = AbstractModel._ID;
    String GUILD = "guild";
    String INVITE = "invite";
    String FEATURES = "features";
  }

  interface Partial extends AbstractPartial {
  }

  @Document(collection = COLLECTION)
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  record Complete(
    @Field(Fields._ID)
    @JsonProperty(Fields._ID)
    @Id ObjectId _id,
    @Field(Fields.GUILD)
    Snowflake guild,
    @Field(Fields.INVITE)
    String invite,
    @Field(Fields.FEATURES)
    Features features
  ) implements AbstractModel {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Features(
      Punishments punishments
    ) {
      @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
      public record Punishments(
        boolean enabled,
        Appeals appeals
      ) {
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public record Appeals(
          boolean enabled,
          Snowflake guild,
          Snowflake everyoneRole,
          Snowflake appealChannelsCategory,
          Snowflake appealThreadsChannel,
          Snowflake appealDiscussionThreadsChannel
        ) {
        }
      }
    }
  }
}
