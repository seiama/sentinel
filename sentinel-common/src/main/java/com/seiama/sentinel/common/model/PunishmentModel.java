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
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "punishments")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NullMarked
public class PunishmentModel implements AbstractModel {
  @MongoPrimaryId
  private ObjectId _id;
  private Snowflake guild;
  private Type type;
  @MongoDate
  private Instant date;
  private Snowflake punisherId;
  private String punisherUsername;
  @Deprecated
  private @Nullable Discriminator punisherDiscriminator;
  private Snowflake punishedId;
  private @Nullable String punishedUsername;
  @Deprecated
  private @Nullable Discriminator punishedDiscriminator;
  private @Nullable String reason;
  private @Nullable Duration duration;
  // an automatic punishment is one created without any intervention from a moderator
  private @Nullable Boolean automatic;
  private @Nullable Boolean expunged;
  // a stale punishment is no longer considered active; if a punishment record banning a user
  // is inserted and that record is then marked as stale; then the user is no longer considered banned
  private @Nullable Boolean stale;
  @MongoDate
  private @Nullable Instant staleAt;
  private @Nullable Snowflake staleById;
  private @Nullable String staleByUsername;
  @Deprecated
  private @Nullable Discriminator staleByDiscriminator;
  private @Nullable String staleReason;
  private @Nullable Boolean staleAutomatic;
  // only non-null if the punishment was appealed
  @MongoId
  private @Nullable ObjectId staleAppeal;
  private @Nullable String importBy;
  private @Nullable String importId;
  @MongoDate
  private @Nullable Instant importAt;
  private @Nullable Snowflake dmNotificationMessageId;
  private @Nullable Snowflake privateNotificationThreadId;

  public PunishmentModel() {
  }

  public PunishmentModel(
    final ObjectId _id,
    final Snowflake guild,
    final Type type,
    final Instant date,
    final Snowflake punisherId,
    final String punisherUsername,
    final @Nullable Discriminator punisherDiscriminator,
    final Snowflake punishedId,
    final @Nullable String punishedUsername,
    final @Nullable Discriminator punishedDiscriminator,
    final @Nullable String reason,
    final @Nullable Duration duration,
    final @Nullable Boolean automatic,
    final @Nullable Boolean expunged,
    final @Nullable Boolean stale,
    final @Nullable Instant staleAt,
    final @Nullable Snowflake staleById,
    final @Nullable String staleByUsername,
    final @Nullable Discriminator staleByDiscriminator,
    final @Nullable String staleReason,
    final @Nullable Boolean staleAutomatic,
    final @Nullable ObjectId staleAppeal,
    final @Nullable String importBy,
    final @Nullable String importId,
    final @Nullable Instant importAt,
    final @Nullable Snowflake dmNotificationMessageId,
    final @Nullable Snowflake privateNotificationThreadId
  ) {
    this._id = _id;
    this.guild = guild;
    this.type = type;
    this.date = date;
    this.punisherId = punisherId;
    this.punisherUsername = punisherUsername;
    this.punisherDiscriminator = punisherDiscriminator;
    this.punishedId = punishedId;
    this.punishedUsername = punishedUsername;
    this.punishedDiscriminator = punishedDiscriminator;
    this.reason = reason;
    this.duration = duration;
    this.automatic = automatic;
    this.expunged = expunged;
    this.stale = stale;
    this.staleAt = staleAt;
    this.staleById = staleById;
    this.staleByUsername = staleByUsername;
    this.staleByDiscriminator = staleByDiscriminator;
    this.staleReason = staleReason;
    this.staleAutomatic = staleAutomatic;
    this.staleAppeal = staleAppeal;
    this.importBy = importBy;
    this.importId = importId;
    this.importAt = importAt;
    this.dmNotificationMessageId = dmNotificationMessageId;
    this.privateNotificationThreadId = privateNotificationThreadId;
  }

  public static PunishmentModel create(
    final Snowflake guild,
    final Type type,
    final Instant date,
    final Optional<User> punisher,
    final User punished,
    final @Nullable String reason,
    final @Nullable Duration duration,
    final boolean automatic
  ) {
    return new PunishmentModel(
      null,
      guild,
      type,
      date,
      punisher
        .map(User::getId)
        .orElse(null),
      punisher
        .map(User::getUsername)
        .orElse(null),
      punisher
        .map(Discriminator::new)
        .orElse(null),
      punished.getId(),
      punished.getUsername(),
      new Discriminator(punished),
      reason,
      duration,
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
      null,
      null
    );
  }

  @Override
  public ObjectId _id() {
    return this._id;
  }

  public Snowflake guild() {
    return this.guild;
  }

  public Type type() {
    return this.type;
  }

  public Instant date() {
    return this.date;
  }

  public Snowflake punisherId() {
    return this.punisherId;
  }

  public String punisherUsername() {
    return this.punisherUsername;
  }

  public @Nullable Discriminator punisherDiscriminator() {
    return this.punisherDiscriminator;
  }

  public UserIdentity punisher() {
    return new UserIdentity(this.punisherId, this.punisherUsername, this.punisherDiscriminator);
  }

  public Snowflake punishedId() {
    return this.punishedId;
  }

  public @Nullable String punishedUsername() {
    return this.punishedUsername;
  }

  public @Nullable Discriminator punishedDiscriminator() {
    return this.punishedDiscriminator;
  }

  public UserIdentity punished() {
    return new UserIdentity(this.punishedId, this.punishedUsername, this.punishedDiscriminator);
  }

  public @Nullable String reason() {
    return this.reason;
  }

  public void setReason(final @Nullable String reason) {
    this.reason = reason;
  }

  public @Nullable Duration duration() {
    return this.duration;
  }

  public @Nullable Boolean automatic() {
    return this.automatic;
  }

  public @Nullable Boolean expunged() {
    return this.expunged;
  }

  public void setExpunged(final @Nullable Boolean expunged) {
    this.expunged = expunged;
  }

  public @Nullable Boolean stale() {
    return this.stale;
  }

  public @Nullable Instant staleAt() {
    return this.staleAt;
  }

  public @Nullable Snowflake staleById() {
    return this.staleById;
  }

  public @Nullable String staleByUsername() {
    return this.staleByUsername;
  }

  public @Nullable Discriminator staleByDiscriminator() {
    return this.staleByDiscriminator;
  }

  public @Nullable String staleReason() {
    return this.staleReason;
  }

  public @Nullable Boolean staleAutomatic() {
    return this.staleAutomatic;
  }

  public @Nullable ObjectId staleAppeal() {
    return this.staleAppeal;
  }

  public void setStale(
    final Optional<User> by,
    final @Nullable String reason,
    final boolean automatic,
    final @Nullable ObjectId appeal
  ) {
    this.stale = true;
    this.staleAt = Instant.now();
    this.staleById = by.map(User::getId).orElse(null);
    this.staleByUsername = by.map(User::getUsername).orElse(null);
    this.staleByDiscriminator = by.map(Discriminator::new).orElse(null);
    this.staleReason = reason;
    this.staleAutomatic = automatic;
    this.staleAppeal = appeal;
  }

  public @Nullable String importBy() {
    return this.importBy;
  }

  public @Nullable String importId() {
    return this.importId;
  }

  public @Nullable Instant importAt() {
    return this.importAt;
  }

  public @Nullable Snowflake dmNotificationMessageId() {
    return this.dmNotificationMessageId;
  }

  public void setDmNotificationMessageId(final @Nullable Snowflake dmNotificationMessageId) {
    this.dmNotificationMessageId = dmNotificationMessageId;
  }

  public @Nullable Snowflake privateNotificationThreadId() {
    return this.privateNotificationThreadId;
  }

  public void setPrivateNotificationThreadId(final @Nullable Snowflake privateNotificationThreadId) {
    this.privateNotificationThreadId = privateNotificationThreadId;
  }

  public boolean wasNotified() {
    return this.dmNotificationMessageId != null || this.privateNotificationThreadId != null;
  }

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
  }

  public enum Type {
    BAN(SharedConstants.COLOR_RED, true, true, new Strings("ban", "banned"), Emojis.DOT_RED),
    KICK(SharedConstants.COLOR_GREY, true, true, new Strings("kick", "kicked"), Emojis.DOT_GREY),
    MUTE(SharedConstants.COLOR_PURPLE, true, false, new Strings("timeout", "timed out"), Emojis.DOT_PURPLE),
    NOTE(SharedConstants.COLOR_BLUE, false, false, new Strings("note", "noted"), Emojis.DOT_BLUE),
    WARN(SharedConstants.COLOR_ORANGE, true, false, new Strings("warn", "warned"), Emojis.DOT_ORANGE);

    private final int color;
    private final boolean notification;
    private final boolean terminal;
    private final Strings strings;
    private final Emoji emoji;

    Type(final int color, final boolean notification, final boolean terminal, final Strings strings, final Emoji emoji) {
      this.color = color;
      this.notification = notification;
      this.terminal = terminal;
      this.strings = strings;
      this.emoji = emoji;
    }

    public int color() {
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
