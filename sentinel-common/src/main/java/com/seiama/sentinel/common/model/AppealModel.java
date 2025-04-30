package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.seiama.sentinel.common.SharedConstants;
import com.seiama.sentinel.common.annotation.MongoDate;
import com.seiama.sentinel.common.annotation.MongoId;
import com.seiama.sentinel.common.annotation.MongoPrimaryId;
import com.seiama.sentinel.common.discord.Emojis;
import discord4j.common.util.Snowflake;
import discord4j.core.object.emoji.Emoji;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.query.Update;

@Document(collection = "appeals")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NullMarked
public class AppealModel implements AbstractModel {
  @MongoPrimaryId
  private ObjectId _id;
  private Snowflake guild;
  @MongoDate
  private Instant date;
  private Snowflake user;
  @MongoId
  private ObjectId punishment;
  private Snowflake appealChannel;
  private Snowflake appealThread;
  private Snowflake appealDiscussionThread;
  private @Nullable Snowflake voteMessage;
  private Map<String, List<Snowflake>> votes; // cannot key by Vote
  @Field("vote_reasons")
  private Map<String, Map<Snowflake, @Nullable String>> voteReasons; // cannot key by Vote
  private  @Nullable Result result;
  private @Nullable String reason;
  @MongoDate
  private @Nullable Instant nextAttemptMayBeMadeAt;

  public AppealModel() {
  }

  public AppealModel(
    final ObjectId _id,
    final Snowflake guild,
    final Instant date,
    final Snowflake user,
    final ObjectId punishment,
    final Snowflake appealChannel,
    final Snowflake appealThread,
    final Snowflake appealDiscussionThread,
    final @Nullable Snowflake voteMessage,
    final Map<String, List<Snowflake>> votes,
    final @Nullable Result result,
    final @Nullable String reason,
    final @Nullable Instant nextAttemptMayBeMadeAt
  ) {
    this._id = _id;
    this.guild = guild;
    this.date = date;
    this.user = user;
    this.punishment = punishment;
    this.appealChannel = appealChannel;
    this.appealThread = appealThread;
    this.appealDiscussionThread = appealDiscussionThread;
    this.voteMessage = voteMessage;
    this.votes = votes;
    this.result = result;
    this.reason = reason;
    this.nextAttemptMayBeMadeAt = nextAttemptMayBeMadeAt;
  }

  @Override
  public ObjectId _id() {
    return this._id;
  }

  public Snowflake guild() {
    return this.guild;
  }

  public Instant date() {
    return this.date;
  }

  public Snowflake user() {
    return this.user;
  }

  public ObjectId punishment() {
    return this.punishment;
  }

  public Snowflake appealChannel() {
    return this.appealChannel;
  }

  public Snowflake appealThread() {
    return this.appealThread;
  }

  public Snowflake appealDiscussionThread() {
    return this.appealDiscussionThread;
  }

  public @Nullable Snowflake voteMessage() {
    return this.voteMessage;
  }

  public void setVoteMessage(final @Nullable Snowflake voteMessage) {
    this.voteMessage = voteMessage;
  }

  public Map<String, List<Snowflake>> votes() {
    return this.votes;
  }

  @SuppressWarnings("MethodName")
  public void setVote(final Snowflake user, final Vote vote, final @Nullable String reason) {
    for (final Map.Entry<String, List<Snowflake>> entry : this.votes.entrySet()) {
      entry.getValue().remove(user);
    }
    this.votes.computeIfAbsent(vote.name(), k -> new ArrayList<>()).add(user);
    this.voteReasons.computeIfAbsent(vote.name(), k -> new HashMap<>()).put(user, reason);
  }

  public @Nullable Result result() {
    return this.result;
  }

  public void setResult(final @Nullable Result result) {
    this.result = result;
  }

  public @Nullable String reason() {
    return this.reason;
  }

  public void setReason(final @Nullable String reason) {
    this.reason = reason;
  }

  public @Nullable Instant nextAttemptMayBeMadeAt() {
    return this.nextAttemptMayBeMadeAt;
  }

  public void setNextAttemptMayBeMadeAt(final @Nullable Instant nextAttemptMayBeMadeAt) {
    this.nextAttemptMayBeMadeAt = nextAttemptMayBeMadeAt;
  }

  public interface Fields {
    String VOTES = "votes";
    String VOTE_REASONS = "vote_reasons";

    static String votes(final Vote vote) {
      return VOTES + "." + vote.name();
    }

    static String voteReasons(final Vote vote, final Snowflake user) {
      return VOTE_REASONS + "." + vote.name() + "." + user.asString();
    }
  }

  public enum Vote {
    YES(true, false, Emojis.YES, new Strings("yes", "Yes")),
    NO(true, false, Emojis.NO, new Strings("no", "No")),
    ABSTAIN(true, false, Emojis.PERSON_SHRUGGING, new Strings("abstain", "Abstain")),
    LATER(true, false, Emojis.CLOCK1, new Strings("later", "Later")),
    VETO(false, true, Emojis.HAMMER, new Strings("veto", "Veto"));

    static final Vote[] VALUES = values();

    private final boolean canVoteWithIfPunisher;
    private final boolean requiresReason;
    private final Emoji emoji;
    private final Strings strings;

    Vote(final boolean canVoteWithIfPunisher, final boolean requiresReason, final Emoji emoji, final Strings strings) {
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

    public Emoji emoji() {
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

  public enum VoteResult {
    NONE,
    YES,
    NO,
    LATER,
    VETO;
  }

  public enum Result {
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
