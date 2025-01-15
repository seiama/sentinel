package com.seiama.sentinel.common.model;

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
  record Complete(
    @Field(Fields._ID)
    @Id ObjectId _id,
    @Field(Fields.GUILD)
    Snowflake guild,
    @Field(Fields.INVITE)
    String invite,
    @Field(Fields.FEATURES)
    Features features
  ) implements AbstractModel {
    public record Features(
      Punishments punishments
    ) {
      public record Punishments(
        boolean enabled,
        Appeals appeals
      ) {
        public record Appeals(
          boolean enabled,
          Snowflake guild,
          Snowflake everyone_role,
          Snowflake appeal_channels_category,
          Snowflake appeal_threads_channel,
          Snowflake appeal_discussion_threads_channel
        ) {
        }
      }
    }
  }
}
