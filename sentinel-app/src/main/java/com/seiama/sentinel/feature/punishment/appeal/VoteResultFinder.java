package com.seiama.sentinel.feature.punishment.appeal;

import com.seiama.sentinel.common.model.PunishmentAppealModel;
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
  static Object2IntMap<PunishmentAppealModel.Vote> mapOf(final Consumer<Object2IntMap<PunishmentAppealModel.Vote>> consumer) {
    final Object2IntMap<PunishmentAppealModel.Vote> votes = new Object2IntArrayMap<>();
    consumer.accept(votes);
    return votes;
  }

  static PunishmentAppealModel.VoteResult resultOf(final Map<PunishmentAppealModel.Vote, List<Snowflake>> votes) {
    return resultOf(mapOf(map -> {
      for (final Map.Entry<PunishmentAppealModel.Vote, List<Snowflake>> entry : votes.entrySet()) {
        map.put(entry.getKey(), entry.getValue().size());
      }
    }));
  }

  @VisibleForTesting
  static PunishmentAppealModel.VoteResult resultOf(final Object2IntMap<PunishmentAppealModel.Vote> votes) {
    // Any veto cancels the vote
    if (votes.containsKey(PunishmentAppealModel.Vote.VETO) && votes.getInt(PunishmentAppealModel.Vote.VETO) > 0) {
      return PunishmentAppealModel.VoteResult.VETO;
    }

    // Require a minimum number of voters
    final int totalVotes = votes.values().intStream().sum();
    if (totalVotes < Appeals.MINIMUM_VOTES) {
      return PunishmentAppealModel.VoteResult.NONE;
    }

    // Require a majority of voters without abstentions
    final int countingVotes = totalVotes - votes.getOrDefault(PunishmentAppealModel.Vote.ABSTAIN, 0);
    return votes.object2IntEntrySet()
      .stream()
      .max(Comparator.comparingInt(Object2IntMap.Entry::getIntValue))
      .map(entry -> {
        final int votesCount = entry.getIntValue();
        final PunishmentAppealModel.Vote voteType = entry.getKey();
        if (votesCount > countingVotes / 2) {
          return voteType.asResult();
        }
        return PunishmentAppealModel.VoteResult.NONE;
      })
      .orElse(PunishmentAppealModel.VoteResult.NONE);
  }
}
