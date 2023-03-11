package com.seiama.sentinel.feature.punishment.appeal;

import com.seiama.sentinel.common.model.AppealModel;
import discord4j.common.util.Snowflake;
import it.unimi.dsi.fastutil.objects.Object2IntArrayMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.jetbrains.annotations.VisibleForTesting;

final class VoteResultFinder {
  private VoteResultFinder() {
  }

  @VisibleForTesting
  static Object2IntMap<AppealModel.Vote> mapOf(final Consumer<Object2IntMap<AppealModel.Vote>> consumer) {
    final Object2IntMap<AppealModel.Vote> votes = new Object2IntArrayMap<>();
    consumer.accept(votes);
    return votes;
  }

  static AppealModel.VoteResult resultOf(final Map<AppealModel.Vote, List<Snowflake>> votes) {
    return resultOf(mapOf(map -> {
      for (final Map.Entry<AppealModel.Vote, List<Snowflake>> entry : votes.entrySet()) {
        map.put(entry.getKey(), entry.getValue().size());
      }
    }));
  }

  @VisibleForTesting
  static AppealModel.VoteResult resultOf(final Object2IntMap<AppealModel.Vote> votes) {
    // Any veto cancels the vote
    if (votes.containsKey(AppealModel.Vote.VETO) && votes.getInt(AppealModel.Vote.VETO) > 0) {
      return AppealModel.VoteResult.VETO;
    }

    // Require a minimum number of voters
    final int totalVotes = votes.values().intStream().sum();
    if (totalVotes < Appeals.MINIMUM_VOTES) {
      return AppealModel.VoteResult.NONE;
    }

    // Require a majority of voters without abstentions
    final int countingVotes = totalVotes - votes.getOrDefault(AppealModel.Vote.ABSTAIN, 0);
    return votes.object2IntEntrySet()
      .stream()
      .max(Comparator.comparingInt(Object2IntMap.Entry::getIntValue))
      .map(entry -> {
        final int votesCount = entry.getIntValue();
        final AppealModel.Vote voteType = entry.getKey();
        if (votesCount > countingVotes / 2) {
          return voteType.asResult();
        }
        return AppealModel.VoteResult.NONE;
      })
      .orElse(AppealModel.VoteResult.NONE);
  }
}
