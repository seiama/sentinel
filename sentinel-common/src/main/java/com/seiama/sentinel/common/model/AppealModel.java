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
  static Update setVote(final Snowflake user, final Vote vote) {
    final Update updates = new Update();
    for (final Vote value : Vote.VALUES) {
      if (value != vote) {
        updates.pull(Fields.votes(value), user);
      }
    }
    updates.push(Fields.votes(vote), user);
    return updates;
  }

  interface Fields {
    String VOTES = "votes";

    static String votes(final Vote vote) {
      return VOTES + "." + vote.name();
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
    YES(Emoji.YES, new Words("yes", "Yes")),
    NO(Emoji.NO, new Words("no", "No")),
    ABSTAIN(Emoji.PERSON_SHRUGGING, new Words("abstain", "Abstain")),
    LATER(Emoji.CLOCK1, new Words("later", "Later")),
    VETO(Emoji.HAMMER, new Words("veto", "Veto"));

    static final Vote[] VALUES = values();

    private final ReactionEmoji emoji;
    private final Words words;

    Vote(final ReactionEmoji emoji, final Words words) {
      this.emoji = emoji;
      this.words = words;
    }

    public static Stream<Vote> all() {
      return Arrays.stream(VALUES);
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

    public Words words() {
      return this.words;
    }

    public record Words(
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
    ACCEPTED(0x17a878, new Words("accepted", "Accepted")),
    DENIED(0xa81747, new Words("denied", "Denied")),
    CANCELLED(0xbb9039, new Words("cancelled", "Cancelled"));

    private final int color;
    private final Words words;

    Result(final int color, final Words words) {
      this.color = color;
      this.words = words;
    }

    public int color() {
      return this.color;
    }

    public Words words() {
      return this.words;
    }

    public record Words(
      String name,
      String nameForStartOfSentence
    ) {
    }
  }
}
