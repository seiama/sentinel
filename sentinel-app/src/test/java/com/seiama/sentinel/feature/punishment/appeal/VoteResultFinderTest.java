package com.seiama.sentinel.feature.punishment.appeal;

import com.seiama.sentinel.common.model.AppealModel;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import org.junit.jupiter.api.Test;

import static com.seiama.sentinel.feature.punishment.appeal.VoteResultFinder.mapOf;
import static com.seiama.sentinel.feature.punishment.appeal.VoteResultFinder.resultOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VoteResultFinderTest {
  @Test
  void testResult() {
    this.testResult(AppealModel.VoteResult.YES, mapOf(votes -> {
      votes.put(AppealModel.Vote.YES, 12);
      votes.put(AppealModel.Vote.NO, 3);
      votes.put(AppealModel.Vote.LATER, 0);
      votes.put(AppealModel.Vote.VETO, 0);
      votes.put(AppealModel.Vote.ABSTAIN, 2);
    }));
    this.testResult(AppealModel.VoteResult.YES, mapOf(votes -> {
      votes.put(AppealModel.Vote.YES, 11);
      votes.put(AppealModel.Vote.NO, 0);
      votes.put(AppealModel.Vote.LATER, 0);
      votes.put(AppealModel.Vote.VETO, 0);
      votes.put(AppealModel.Vote.ABSTAIN, 0);
    }));
    this.testResult(AppealModel.VoteResult.YES, mapOf(votes -> {
      votes.put(AppealModel.Vote.YES, 7);
      votes.put(AppealModel.Vote.NO, 1);
      votes.put(AppealModel.Vote.LATER, 0);
      votes.put(AppealModel.Vote.VETO, 0);
      votes.put(AppealModel.Vote.ABSTAIN, 0);
    }));
    this.testResult(AppealModel.VoteResult.YES, mapOf(votes -> {
      votes.put(AppealModel.Vote.YES, 3);
      votes.put(AppealModel.Vote.NO, 0);
      votes.put(AppealModel.Vote.LATER, 0);
      votes.put(AppealModel.Vote.VETO, 0);
      votes.put(AppealModel.Vote.ABSTAIN, 0);
    }));

    this.testResult(AppealModel.VoteResult.NO, mapOf(votes -> {
      votes.put(AppealModel.Vote.YES, 0);
      votes.put(AppealModel.Vote.NO, 6);
      votes.put(AppealModel.Vote.LATER, 0);
      votes.put(AppealModel.Vote.VETO, 0);
      votes.put(AppealModel.Vote.ABSTAIN, 0);
    }));

    this.testResult(AppealModel.VoteResult.LATER, mapOf(votes -> {
      votes.put(AppealModel.Vote.YES, 0);
      votes.put(AppealModel.Vote.NO, 2);
      votes.put(AppealModel.Vote.LATER, 8);
      votes.put(AppealModel.Vote.VETO, 0);
      votes.put(AppealModel.Vote.ABSTAIN, 1);
    }));

    this.testResult(AppealModel.VoteResult.VETO, mapOf(votes -> {
      votes.put(AppealModel.Vote.YES, 8);
      votes.put(AppealModel.Vote.NO, 2);
      votes.put(AppealModel.Vote.LATER, 1);
      votes.put(AppealModel.Vote.VETO, 1);
      votes.put(AppealModel.Vote.ABSTAIN, 0);
    }));

    this.testResult(AppealModel.VoteResult.NONE, mapOf(votes -> {
      votes.put(AppealModel.Vote.YES, 0);
      votes.put(AppealModel.Vote.NO, 0);
      votes.put(AppealModel.Vote.LATER, 0);
      votes.put(AppealModel.Vote.VETO, 0);
      votes.put(AppealModel.Vote.ABSTAIN, 0);
    }));
    this.testResult(AppealModel.VoteResult.YES, mapOf(votes -> {
      votes.put(AppealModel.Vote.YES, 6);
      votes.put(AppealModel.Vote.NO, 5);
      votes.put(AppealModel.Vote.LATER, 0);
      votes.put(AppealModel.Vote.VETO, 0);
      votes.put(AppealModel.Vote.ABSTAIN, 0);
    }));
    this.testResult(AppealModel.VoteResult.NO, mapOf(votes -> {
      votes.put(AppealModel.Vote.YES, 5);
      votes.put(AppealModel.Vote.NO, 6);
      votes.put(AppealModel.Vote.LATER, 0);
      votes.put(AppealModel.Vote.VETO, 0);
      votes.put(AppealModel.Vote.ABSTAIN, 0);
    }));
  }

  private void testResult(final AppealModel.VoteResult result, final Object2IntMap<AppealModel.Vote> votes) {
    assertEquals(result, resultOf(votes), votes.toString());
  }
}
