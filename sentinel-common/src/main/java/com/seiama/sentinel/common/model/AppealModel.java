package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.seiama.sentinel.common.discord.Emoji;
import discord4j.common.util.Snowflake;
import discord4j.core.object.reaction.ReactionEmoji;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

public interface AppealModel {
  String COLLECTION = "appeals";

  static String voteKey(final Vote vote) {
    return Fields.VOTES + "." + vote.name();
  }

  interface Fields {
    @SuppressWarnings("ConstantName")
    String _ID = AbstractModel._ID;
    String GUILD = "guild";
    String DATE = "date";
    String USER = "user";
    String PUNISHMENT = "punishment";
    String APPEAL_CHANNEL = "appeal_channel";
    String APPEAL_THREAD = "appeal_thread";
    String APPEAL_DISCUSSION_THREAD = "appeal_discussion_thread";
    String VOTE_MESSAGE = "vote_message";
    String VOTES = "votes";
    String RESULT = "result";
    String REASON = "reason";
    String NEXT_ATTEMPT_MAY_BE_MADE_AT = "next_attempt_may_be_made_at";
  }

  interface Partial extends AbstractPartial {
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    @SuppressWarnings("EmptyLineSeparator")
    interface Close extends Partial {
      @JsonProperty Result result();
      @JsonProperty @Nullable String reason();
      @JsonProperty @Nullable Instant nextAttemptMayBeMadeAt();
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    interface VoteMessage extends Partial {
      @JsonInclude(value = JsonInclude.Include.NON_NULL) @Nullable Snowflake voteMessage();
    }
  }

  @Document(collection = COLLECTION)
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  record Complete(
    @Field(Fields._ID)
    @JsonProperty(Fields._ID)
    @Id ObjectId _id,
    Snowflake guild,
    Instant date,
    Snowflake user,
    ObjectId punishment,
    Snowflake appealChannel,
    Snowflake appealThread,
    Snowflake appealDiscussionThread,
    @Nullable Snowflake voteMessage,
    Map<String, List<Snowflake>> votes, // cannot key by Vote
    Result result,
    @Nullable String reason,
    @Nullable Instant nextAttemptMayBeMadeAt
  ) implements AbstractModel {
  }

  enum Vote {
    YES(Emoji.YES, new Words("yes", "Yes")),
    NO(Emoji.NO, new Words("no", "No")),
    ABSTAIN(Emoji.PERSON_SHRUGGING, new Words("abstain", "Abstain")),
    LATER(Emoji.CLOCK1, new Words("later", "Later")),
    VETO(Emoji.HAMMER, new Words("veto", "Veto"));

    private final ReactionEmoji emoji;
    private final Words words;

    Vote(final ReactionEmoji emoji, final Words words) {
      this.emoji = emoji;
      this.words = words;
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
