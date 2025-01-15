package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.seiama.sentinel.common.annotation.MongoPrimaryId;
import discord4j.common.util.Snowflake;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

public interface GuildModel {
  String COLLECTION = "guilds";

  interface Partial extends AbstractPartial {
  }

  @Document(collection = COLLECTION)
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  record Complete(
    @MongoPrimaryId ObjectId _id,
    Snowflake guild,
    String invite,
    Features features
  ) implements AbstractModel {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Features(
      Punishments punishments,
      Factoids factoids
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

      @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
      public record Factoids(
        boolean enabled
      ) {
      }
    }
  }
}
