package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.seiama.sentinel.common.discord.Emoji;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import java.time.Instant;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

public interface PunishmentModel {
  String COLLECTION = "punishments";

  interface Fields {
    @SuppressWarnings("ConstantName")
    String _ID = AbstractModel._ID;
    String GUILD = "guild";
    String TYPE = "type";
    String DATE = "date";
    String PUNISHER_ID = "punisher_id";
    String PUNISHER_USERNAME = "punisher_username";
    String PUNISHER_DISCRIMINATOR = "punisher_discriminator";
    String PUNISHED_ID = "punished_id";
    String PUNISHED_USERNAME = "punished_username";
    String PUNISHED_DISCRIMINATOR = "punished_discriminator";
    String REASON = "reason";
    String AUTOMATIC = "automatic";
    String EXPUNGED = "expunged";
    String STALE = "stale";
    String STALE_AUTOMATIC = "stale_automatic";
    String STALE_AT = "stale_at";
    String STALE_BY_ID = "stale_by_id";
    String STALE_BY_USERNAME = "stale_by_username";
    String STALE_BY_DISCRIMINATOR = "stale_by_discriminator";
    String STALE_REASON = "stale_reason";
    String IMPORT_BY = "import_by";
    String IMPORT_ID = "import_id";
    String IMPORT_AT = "import_at";
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
      @JsonProperty @Nullable Boolean stale();
      @JsonProperty Instant staleAt();
      @JsonProperty Snowflake staleById();
      @JsonProperty String staleByUsername();
      @JsonProperty String staleByDiscriminator();
      @JsonProperty @Nullable String staleReason();
      @JsonProperty @Nullable Boolean staleAutomatic();
      @JsonProperty @Nullable ObjectId staleAppeal();
    }
  }

  @Document(collection = COLLECTION)
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  record Complete(
    @Field(Fields._ID)
    @JsonProperty(Fields._ID)
    ObjectId _id,
    Snowflake guild,
    Type type,
    Instant date,
    Snowflake punisherId,
    String punisherUsername,
    String punisherDiscriminator,
    Snowflake punishedId,
    @Nullable String punishedUsername,
    @Nullable String punishedDiscriminator,
    @Nullable String reason,
    // an automatic punishment is one created without any intervention from a moderator
    @Nullable Boolean automatic,
    @Nullable Boolean expunged,
    // a stale punishment is no longer considered active; if a punishment record banning a user
    // is inserted and that record is then marked as stale, then the user is no longer considered banned
    @Nullable Boolean stale,
    @Nullable Instant staleAt,
    @Nullable Snowflake staleById,
    @Nullable String staleByUsername,
    @Nullable String staleByDiscriminator,
    @Nullable String staleReason,
    @Nullable Boolean staleAutomatic,
    // only non-null if the punishment was appealed
    @Nullable ObjectId staleAppeal,
    @Nullable String importBy,
    @Nullable String importId,
    @Nullable Instant importAt
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
        punisher.getDiscriminator(),
        punished.getId(),
        punished.getUsername(),
        punished.getDiscriminator(),
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
        null
      );
    }
  }

  enum Type {
    BAN(0xe22966, new Words("ban", "banned"), Emoji.DOT_RED),
    KICK(0xe24929, new Words("kick", "kicked"), Emoji.DOT_GREY),
    MUTE(0xe2a629, new Words("mute", "muted"), Emoji.DOT_PURPLE),
    WARN(0x2965e2, new Words("warn", "warned"), Emoji.DOT_ORANGE);

    private final int color;
    private final Words words;
    private final ReactionEmoji emoji;

    Type(final int color, final Words words, final ReactionEmoji emoji) {
      this.color = color;
      this.words = words;
      this.emoji = emoji;
    }

    public int color() {
      return this.color;
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
