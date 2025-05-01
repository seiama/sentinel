package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.seiama.sentinel.common.SharedConstants;
import com.seiama.sentinel.common.annotation.MongoDate;
import com.seiama.sentinel.common.annotation.MongoId;
import com.seiama.sentinel.common.annotation.MongoPrimaryId;
import com.seiama.sentinel.common.discord.Emojis;
import com.seiama.sentinel.common.jackson.ObjectIdExtendedJsonSerializer;
import discord4j.common.util.Snowflake;
import discord4j.core.object.emoji.Emoji;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.mongodb.core.mapping.Document;

@NullMarked
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
      static Stale of(
        final Optional<User> by,
        final @Nullable String reason,
        final boolean automatic,
        final @Nullable ObjectId appeal
      ) {
        return of(
          by.map(User::getId),
          by.map(User::getUsername),
          by.map(Discriminator::new),
          reason,
          automatic,
          appeal
        );
      }

      static Stale of(
        final Optional<Snowflake> byId,
        final Optional<String> byUsername,
        final Optional<Discriminator> byDiscriminator,
        final @Nullable String reason,
        final boolean automatic,
        final @Nullable ObjectId appeal
      ) {
        return new Stale() {
          @Override
          public Boolean stale() {
            return true;
          }

          @Override
          public Instant staleAt() {
            return Instant.now();
          }

          @Override
          public Snowflake staleById() {
            return byId.orElse(null);
          }

          @Override
          public String staleByUsername() {
            return byUsername.orElse(null);
          }

          @Override
          public Discriminator staleByDiscriminator() {
            return byDiscriminator.orElse(null);
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
      @JsonProperty
      Discriminator staleByDiscriminator();
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

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    interface PrivateThreadNotified extends Partial {
      @JsonProperty Snowflake privateNotificationThreadId();
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
    @Nullable Discriminator punisherDiscriminator,
    Snowflake punishedId,
    @Nullable String punishedUsername,
    @Deprecated
    @Nullable Discriminator punishedDiscriminator,
    @Nullable String reason,
    @Nullable Duration duration,
    // an automatic punishment is one created without any intervention from a moderator
    @Nullable Boolean automatic,
    @Nullable Boolean permanent,
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
    @Nullable Snowflake dmNotificationMessageId,
    @Nullable Snowflake privateNotificationThreadId
  ) implements AbstractModel, Partial.Reason, Partial.Expunged, Partial.Stale {
    public UserIdentity punisher() {
      return new UserIdentity(this.punisherId, this.punisherUsername, this.punisherDiscriminator);
    }

    public UserIdentity punished() {
      return new UserIdentity(this.punishedId, this.punishedUsername, this.punishedDiscriminator);
    }

    public static Complete create(
      final Snowflake guild,
      final Type type,
      final Instant date,
      final Optional<UserIdentity> punisher,
      final UserIdentity punished,
      final @Nullable String reason,
      final @Nullable Duration duration,
      final boolean automatic
    ) {
      return new Complete(
        null,
        guild,
        type,
        date,
        punisher
          .map(UserIdentity::id)
          .orElse(null),
        punisher
          .map(UserIdentity::username)
          .orElse(null),
        punisher
          .map(UserIdentity::discriminator)
          .orElse(null),
        punished.id(),
        punished.username(),
        punished.discriminator(),
        reason,
        duration,
        automatic,
        false,
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
        null,
        null
      );
    }

    public boolean wasNotified() {
      return this.dmNotificationMessageId != null || this.privateNotificationThreadId != null;
    }
  }

  enum Type {
    BAN(SharedConstants.COLOR_RED, true, true, new Strings("ban", "banned"), Emojis.DOT_RED),
    KICK(SharedConstants.COLOR_GREY, true, true, new Strings("kick", "kicked"), Emojis.DOT_GREY),
    MUTE(SharedConstants.COLOR_PURPLE, true, false, new Strings("timeout", "timed out"), Emojis.DOT_PURPLE),
    NOTE(SharedConstants.COLOR_BLUE, false, false, new Strings("note", "noted"), Emojis.DOT_BLUE),
    WARN(SharedConstants.COLOR_ORANGE, true, false, new Strings("warn", "warned"), Emojis.DOT_ORANGE);

    private final Color color;
    private final boolean notification;
    private final boolean terminal;
    private final Strings strings;
    private final Emoji emoji;

    Type(final Color color, final boolean notification, final boolean terminal, final Strings strings, final Emoji emoji) {
      this.color = color;
      this.notification = notification;
      this.terminal = terminal;
      this.strings = strings;
      this.emoji = emoji;
    }

    public Color color() {
      return this.color;
    }

    public boolean notification() {
      return this.notification;
    }

    public boolean terminal() {
      return this.terminal;
    }

    public Strings strings() {
      return this.strings;
    }

    public Emoji emoji() {
      return this.emoji;
    }

    public record Strings(
      String name,
      String actioned
    ) {
    }
  }
}
