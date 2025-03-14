package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.seiama.sentinel.common.IsEnabled;
import com.seiama.sentinel.common.annotation.MongoPrimaryId;
import discord4j.common.util.Snowflake;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.mongodb.core.mapping.Document;

@NullMarked
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
    public <F extends IsEnabled> boolean featureEnabled(final Function<Features, F> featureGetter) {
      final @Nullable F feature = featureGetter.apply(this.features);
      return feature != null && feature.enabled();
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record Features(
      Punishments punishments,
      Factoids factoids,
      Javadocs javadocs,
      ModMail modmail,
      Logging logging
    ) {
      @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
      public record Punishments(
        boolean enabled,
        Permissions permissions,
        Snowflake logChannel,
        Snowflake privateThreadNotificationChannel,
        Appeals appeals
      ) implements IsEnabled {
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public record Permissions(
          Set<Snowflake> punish,
          Set<Snowflake> exempt
        ) {
        }

        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public record Appeals(
          boolean enabled,
          Snowflake guild,
          Snowflake everyoneRole,
          Snowflake appealChannelsCategory,
          Snowflake appealThreadsChannel,
          Snowflake appealDiscussionThreadsChannel
        ) implements IsEnabled {
        }
      }

      @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
      public record Factoids(
        boolean enabled
      ) implements IsEnabled {
      }

      @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
      public record JavaDocs(
        boolean enabled
      ) implements IsEnabled {
      }

      @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
      public record ModMail(
        boolean enabled,
        Snowflake notificationChannel,
        Snowflake threadChannel
      ) implements IsEnabled {
      }

      @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
      public record Logging(
        boolean enabled,
        Map<Snowflake, List<Event>> mapping
      ) implements IsEnabled {
        public enum Event {
          MEMBER_JOIN,
          MEMBER_LEAVE,
        }
      }
    }
  }
}
