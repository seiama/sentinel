package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.seiama.sentinel.common.SharedConstants;
import com.seiama.sentinel.common.annotation.MongoDate;
import com.seiama.sentinel.common.annotation.MongoPrimaryId;
import com.seiama.sentinel.common.discord.Emojis;
import com.seiama.sentinel.common.jackson.InstantExtendedJsonSerializer;
import discord4j.common.util.Snowflake;
import discord4j.core.object.emoji.Emoji;
import discord4j.rest.util.Color;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;


@NullMarked
public interface ApplicationModel {
  String COLLECTION = "applications";

  @SuppressWarnings("MethodName")
  static Update setVote(final Snowflake user, final Vote vote) {
    final Update updates = new Update();
    for (final Vote value : Vote.VALUES) {
      if (value != vote) {
        updates.pull(Fields.votes(value), user);
      }
    }
    updates.addToSet(Fields.votes(vote), user);
    return updates;
  }

  interface Fields {
    String VOTES = "votes";
    String VOTE_REASONS = "vote_reasons";

    static String votes(final Vote vote) {
      return VOTES + "." + vote.name();
    }
  }

  interface Partial extends AbstractPartial {
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @SuppressWarnings("EmptyLineSeparator")
    interface Close extends Partial {
      @JsonProperty
      Result result();

      @JsonProperty
      @Nullable String reason();

      @JsonSerialize(using = InstantExtendedJsonSerializer.class)
      @JsonProperty
      @MongoDate
      @Nullable Instant nextAttemptMayBeMadeAt();
    }
  }

  @Document(collection = COLLECTION)
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  record Complete(
    @MongoPrimaryId ObjectId _id,
    Snowflake guild,
    @MongoDate Instant date,
    Snowflake user,
    Snowflake pollId,
    String githubProfile,
    String prLink,
    Map<String, List<Snowflake>> votes, // cannot key by Vote
    @Nullable Result result,
    @MongoDate @Nullable Instant nextAttemptMayBeMadeAt
  ) implements AbstractModel {
  }

  enum Vote {
    YES(false, Emojis.YES, "Yes"),
    NO(false, Emojis.NO, "No"),
    FAST_TRACK(true, Emojis.HAMMER,"Veto (Leadership Only)");

    static final Vote[] VALUES = values();

    private final boolean leadershipOnly;
    private final Emoji emoji;
    private final String label;

    Vote(final boolean leadershipOnly, final Emoji emoji, final String label) {
      this.leadershipOnly = leadershipOnly;
      this.emoji = emoji;
      this.label = label;
    }

    public static Stream<Vote> all() {
      return Arrays.stream(VALUES);
    }

    public boolean leadershipOnly() {
      return this.leadershipOnly;
    }

    public VoteResult asResult() {
      return switch (this) {
        case YES -> VoteResult.YES;
        case NO -> VoteResult.NO;
        case FAST_TRACK -> VoteResult.FAST_TRACK;
      };
    }

    public Emoji emoji() {
      return this.emoji;
    }

    public String label() {
      return this.label;
    }
  }

  enum VoteResult {
    NONE,
    YES,
    NO,
    FAST_TRACK
  }

  enum Result {
    ACCEPTED(SharedConstants.COLOR_GREEN, new Strings("accepted", "Accepted")),
    DENIED(SharedConstants.COLOR_RED, new Strings("denied", "Denied")),
    FAST_TRACKED(SharedConstants.COLOR_ORANGE, new Strings("fast_tracked", "Fast Tracked"));

    private final Color color;
    private final Strings strings;

    Result(final Color color, final Strings strings) {
      this.color = color;
      this.strings = strings;
    }

    public Color color() {
      return this.color;
    }

    public Strings strings() {
      return this.strings;
    }

    public record Strings(
      String name,
      String nameForStartOfSentence
    ) {
    }
  }
}
