package com.seiama.sentinel.feature.punishment.appeal;

import com.seiama.sentinel.common.model.PunishmentAppealModel;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.junit.jupiter.api.Test;

import static com.seiama.sentinel.feature.punishment.appeal.VoteResultFinder.mapOf;
import static com.seiama.sentinel.feature.punishment.appeal.VoteResultFinder.resultOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VoteResultFinderTest {
  @Test
  void testResult() {
    this.testResult(PunishmentAppealModel.VoteResult.YES, mapOf(votes -> {
      votes.put(PunishmentAppealModel.Vote.YES, 12);
      votes.put(PunishmentAppealModel.Vote.NO, 3);
      votes.put(PunishmentAppealModel.Vote.LATER, 0);
      votes.put(PunishmentAppealModel.Vote.VETO, 0);
      votes.put(PunishmentAppealModel.Vote.ABSTAIN, 2);
    }));
    this.testResult(PunishmentAppealModel.VoteResult.YES, mapOf(votes -> {
      votes.put(PunishmentAppealModel.Vote.YES, 11);
      votes.put(PunishmentAppealModel.Vote.NO, 0);
      votes.put(PunishmentAppealModel.Vote.LATER, 0);
      votes.put(PunishmentAppealModel.Vote.VETO, 0);
      votes.put(PunishmentAppealModel.Vote.ABSTAIN, 0);
    }));
    this.testResult(PunishmentAppealModel.VoteResult.YES, mapOf(votes -> {
      votes.put(PunishmentAppealModel.Vote.YES, 7);
      votes.put(PunishmentAppealModel.Vote.NO, 1);
      votes.put(PunishmentAppealModel.Vote.LATER, 0);
      votes.put(PunishmentAppealModel.Vote.VETO, 0);
      votes.put(PunishmentAppealModel.Vote.ABSTAIN, 0);
    }));
    this.testResult(PunishmentAppealModel.VoteResult.YES, mapOf(votes -> {
      votes.put(PunishmentAppealModel.Vote.YES, 3);
      votes.put(PunishmentAppealModel.Vote.NO, 0);
      votes.put(PunishmentAppealModel.Vote.LATER, 0);
      votes.put(PunishmentAppealModel.Vote.VETO, 0);
      votes.put(PunishmentAppealModel.Vote.ABSTAIN, 0);
    }));

    this.testResult(PunishmentAppealModel.VoteResult.NO, mapOf(votes -> {
      votes.put(PunishmentAppealModel.Vote.YES, 0);
      votes.put(PunishmentAppealModel.Vote.NO, 6);
      votes.put(PunishmentAppealModel.Vote.LATER, 0);
      votes.put(PunishmentAppealModel.Vote.VETO, 0);
      votes.put(PunishmentAppealModel.Vote.ABSTAIN, 0);
    }));

    this.testResult(PunishmentAppealModel.VoteResult.LATER, mapOf(votes -> {
      votes.put(PunishmentAppealModel.Vote.YES, 0);
      votes.put(PunishmentAppealModel.Vote.NO, 2);
      votes.put(PunishmentAppealModel.Vote.LATER, 8);
      votes.put(PunishmentAppealModel.Vote.VETO, 0);
      votes.put(PunishmentAppealModel.Vote.ABSTAIN, 1);
    }));

    this.testResult(PunishmentAppealModel.VoteResult.VETO, mapOf(votes -> {
      votes.put(PunishmentAppealModel.Vote.YES, 8);
      votes.put(PunishmentAppealModel.Vote.NO, 2);
      votes.put(PunishmentAppealModel.Vote.LATER, 1);
      votes.put(PunishmentAppealModel.Vote.VETO, 1);
      votes.put(PunishmentAppealModel.Vote.ABSTAIN, 0);
    }));

    this.testResult(PunishmentAppealModel.VoteResult.NONE, mapOf(votes -> {
      votes.put(PunishmentAppealModel.Vote.YES, 0);
      votes.put(PunishmentAppealModel.Vote.NO, 0);
      votes.put(PunishmentAppealModel.Vote.LATER, 0);
      votes.put(PunishmentAppealModel.Vote.VETO, 0);
      votes.put(PunishmentAppealModel.Vote.ABSTAIN, 0);
    }));
    this.testResult(PunishmentAppealModel.VoteResult.YES, mapOf(votes -> {
      votes.put(PunishmentAppealModel.Vote.YES, 6);
      votes.put(PunishmentAppealModel.Vote.NO, 5);
      votes.put(PunishmentAppealModel.Vote.LATER, 0);
      votes.put(PunishmentAppealModel.Vote.VETO, 0);
      votes.put(PunishmentAppealModel.Vote.ABSTAIN, 0);
    }));
    this.testResult(PunishmentAppealModel.VoteResult.NO, mapOf(votes -> {
      votes.put(PunishmentAppealModel.Vote.YES, 5);
      votes.put(PunishmentAppealModel.Vote.NO, 6);
      votes.put(PunishmentAppealModel.Vote.LATER, 0);
      votes.put(PunishmentAppealModel.Vote.VETO, 0);
      votes.put(PunishmentAppealModel.Vote.ABSTAIN, 0);
    }));
  }

  private void testResult(final PunishmentAppealModel.VoteResult result, final Object2IntMap<PunishmentAppealModel.Vote> votes) {
    assertEquals(result, resultOf(votes), votes.toString());
  }
}
