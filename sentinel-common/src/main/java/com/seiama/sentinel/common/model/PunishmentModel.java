package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.seiama.sentinel.common.annotation.MongoDate;
import com.seiama.sentinel.common.annotation.MongoId;
import com.seiama.sentinel.common.annotation.MongoPrimaryId;
import com.seiama.sentinel.common.discord.Emoji;
import com.seiama.sentinel.common.jackson.ObjectIdExtendedJsonSerializer;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import java.time.Instant;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.mongodb.core.mapping.Document;

public interface PunishmentModel {
  String COLLECTION = "punishments";

  interface Partial extends AbstractPartial {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    interface Reason extends Partial {
      @JsonProperty @Nullable String reason();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    interface Expunged extends Partial {
      @JsonProperty @Nullable Boolean expunged();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @SuppressWarnings("EmptyLineSeparator")
    interface Stale extends Partial {
      static @NotNull Stale of(final @NotNull User by, final @Nullable String reason, final boolean automatic, final @Nullable ObjectId appeal) {
        return new Stale() {
          @Override
          public @NotNull Boolean stale() {
            return true;
          }

          @Override
          public Instant staleAt() {
            return Instant.now();
          }

          @Override
          public Snowflake staleById() {
            return by.getId();
          }

          @Override
          public String staleByUsername() {
            return by.getUsername();
          }

          @Override
          public Discriminator staleByDiscriminator() {
            return new Discriminator(by.getDiscriminator());
          }

          @Override
          public String staleReason() {
            return reason;
          }

          @Override
          public Boolean staleAutomatic() {
            return automatic;
          }

          @Override
          public @Nullable ObjectId staleAppeal() {
            return appeal;
          }
        };
      }

      @JsonProperty @Nullable Boolean stale();
      @JsonProperty Instant staleAt();
      @JsonProperty Snowflake staleById();
      @JsonProperty String staleByUsername();
      @JsonProperty Discriminator staleByDiscriminator();
      @JsonProperty @Nullable String staleReason();
      @JsonProperty @Nullable Boolean staleAutomatic();
      @JsonSerialize(using = ObjectIdExtendedJsonSerializer.class)
      @JsonProperty @MongoId @Nullable ObjectId staleAppeal();
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    interface DirectMessageNotified extends Partial {
      @JsonProperty Snowflake dmNotificationMessageId();
    }
  }

  @Document(collection = COLLECTION)
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  record Complete(
    @MongoPrimaryId ObjectId _id,
    Snowflake guild,
    Type type,
    @MongoDate Instant date,
    Snowflake punisherId,
    String punisherUsername,
    @Deprecated
    Discriminator punisherDiscriminator,
    Snowflake punishedId,
    @Nullable String punishedUsername,
    @Deprecated
    @Nullable Discriminator punishedDiscriminator,
    @Nullable String reason,
    // an automatic punishment is one created without any intervention from a moderator
    @Nullable Boolean automatic,
    @Nullable Boolean expunged,
    // a stale punishment is no longer considered active; if a punishment record banning a user
    // is inserted and that record is then marked as stale, then the user is no longer considered banned
    @Nullable Boolean stale,
    @MongoDate @Nullable Instant staleAt,
    @Nullable Snowflake staleById,
    @Nullable String staleByUsername,
    @Deprecated
    @Nullable Discriminator staleByDiscriminator,
    @Nullable String staleReason,
    @Nullable Boolean staleAutomatic,
    // only non-null if the punishment was appealed
    @MongoId @Nullable ObjectId staleAppeal,
    @Nullable String importBy,
    @Nullable String importId,
    @MongoDate @Nullable Instant importAt,
    @Nullable Snowflake dmNotificationMessageId
  ) implements AbstractModel, Partial.Reason, Partial.Expunged, Partial.Stale {
    public static @NotNull Complete create(
      final @NotNull Snowflake guild,
      final @NotNull Type type,
      final @NotNull Instant date,
      final @NotNull User punisher,
      final @NotNull User punished,
      final @Nullable String reason,
      final boolean automatic
    ) {
      return new Complete(
        null,
        guild,
        type,
        date,
        punisher.getId(),
        punisher.getUsername(),
        new Discriminator(punisher.getDiscriminator()),
        punished.getId(),
        punished.getUsername(),
        new Discriminator(punished.getDiscriminator()),
        reason,
        automatic,
        false,
        false,
        null,
        null,
        null,
        null,
        null,
        false,
        null,
        null,
        null,
        null,
        null
      );
    }
  }

  enum Type {
    BAN(0xf04747, true, new Words("ban", "banned"), Emoji.DOT_RED),
    KICK(0xb5988e, true, new Words("kick", "kicked"), Emoji.DOT_GREY),
    MUTE(0x9b59b6, true, new Words("mute", "muted"), Emoji.DOT_PURPLE),
    NOTE(0x3498db, false, new Words("note", "noted"), Emoji.DOT_BLUE),
    WARN(0xfaa61a, true, new Words("warn", "warned"), Emoji.DOT_ORANGE);

    private final int color;
    private final boolean notification;
    private final Words words;
    private final ReactionEmoji emoji;

    Type(final int color, final boolean notification, final Words words, final ReactionEmoji emoji) {
      this.color = color;
      this.notification = notification;
      this.words = words;
      this.emoji = emoji;
    }

    public int color() {
      return this.color;
    }

    public boolean notification() {
      return this.notification;
    }

    public Words words() {
      return this.words;
    }

    public ReactionEmoji emoji() {
      return this.emoji;
    }

    public record Words(
      String name,
      String actioned
    ) {
    }
  }
}
