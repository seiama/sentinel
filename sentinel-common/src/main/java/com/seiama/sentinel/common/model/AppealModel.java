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
import com.seiama.sentinel.common.discord.Emoji;
import com.seiama.sentinel.common.jackson.InstantExtendedJsonSerializer;
import discord4j.common.util.Snowflake;
import discord4j.core.object.reaction.ReactionEmoji;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.query.Update;

public interface AppealModel {
  String COLLECTION = "appeals";

  @SuppressWarnings("MethodName")
  static Update setVote(final Snowflake user, final Vote vote, final @Nullable String reason) {
    final Update updates = new Update();
    for (final Vote value : Vote.VALUES) {
      if (value != vote) {
        updates.pull(Fields.votes(value), user);
      }
    }
    updates.addToSet(Fields.votes(vote), user);
    updates.set(Fields.voteReasons(vote, user), reason);
    return updates;
  }

  interface Fields {
    String VOTES = "votes";
    String VOTE_REASONS = "vote_reasons";

    static String votes(final Vote vote) {
      return VOTES + "." + vote.name();
    }

    static String voteReasons(final Vote vote, final Snowflake user) {
      return VOTE_REASONS + "." + vote.name() + "." + user.asString();
    }
  }

  interface Partial extends AbstractPartial {
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @SuppressWarnings("EmptyLineSeparator")
    interface Close extends Partial {
      @JsonProperty Result result();
      @JsonProperty @Nullable String reason();
      @JsonSerialize(using = InstantExtendedJsonSerializer.class)
      @JsonProperty @MongoDate @Nullable Instant nextAttemptMayBeMadeAt();
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    interface VoteMessage extends Partial {
      @JsonInclude(value = JsonInclude.Include.NON_NULL) @Nullable Snowflake voteMessage();
    }
  }

  @Document(collection = COLLECTION)
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  record Complete(
    @MongoPrimaryId ObjectId _id,
    Snowflake guild,
    @MongoDate Instant date,
    Snowflake user,
    @MongoId ObjectId punishment,
    Snowflake appealChannel,
    Snowflake appealThread,
    Snowflake appealDiscussionThread,
    @Nullable Snowflake voteMessage,
    Map<String, List<Snowflake>> votes, // cannot key by Vote
    Result result,
    @Nullable String reason,
    @MongoDate @Nullable Instant nextAttemptMayBeMadeAt
  ) implements AbstractModel {
  }

  enum Vote {
    YES(true, false, Emoji.YES, new Strings("yes", "Yes")),
    NO(true, false, Emoji.NO, new Strings("no", "No")),
    ABSTAIN(true, false, Emoji.PERSON_SHRUGGING, new Strings("abstain", "Abstain")),
    LATER(true, false, Emoji.CLOCK1, new Strings("later", "Later")),
    VETO(false, true, Emoji.HAMMER, new Strings("veto", "Veto"));

    static final Vote[] VALUES = values();

    private final boolean canVoteWithIfPunisher;
    private final boolean requiresReason;
    private final ReactionEmoji emoji;
    private final Strings strings;

    Vote(final boolean canVoteWithIfPunisher, final boolean requiresReason, final ReactionEmoji emoji, final Strings strings) {
      this.canVoteWithIfPunisher = canVoteWithIfPunisher;
      this.requiresReason = requiresReason;
      this.emoji = emoji;
      this.strings = strings;
    }

    public static Stream<Vote> all() {
      return Arrays.stream(VALUES);
    }

    public boolean canVoteWithIfPunisher() {
      return this.canVoteWithIfPunisher;
    }

    public boolean requiresReason() {
      return this.requiresReason;
    }

    public VoteResult asResult() {
      return switch (this) {
        case YES -> VoteResult.YES;
        case NO -> VoteResult.NO;
        case LATER -> VoteResult.LATER;
        case VETO -> VoteResult.VETO;
        case ABSTAIN -> VoteResult.NONE;
      };
    }

    public ReactionEmoji emoji() {
      return this.emoji;
    }

    public Strings strings() {
      return this.strings;
    }

    public record Strings(
      String button,
      String name
    ) {
    }
  }

  enum VoteResult {
    NONE,
    YES,
    NO,
    LATER,
    VETO;
  }

  enum Result {
    ACCEPTED(SharedConstants.COLOR_GREEN, new Strings("accepted", "Accepted")),
    DENIED(SharedConstants.COLOR_RED, new Strings("denied", "Denied")),
    CANCELLED(SharedConstants.COLOR_ORANGE, new Strings("cancelled", "Cancelled"));

    private final int color;
    private final Strings strings;

    Result(final int color, final Strings strings) {
      this.color = color;
      this.strings = strings;
    }

    public int color() {
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
