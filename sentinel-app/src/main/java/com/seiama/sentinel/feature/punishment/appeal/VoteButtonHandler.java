package com.seiama.sentinel.feature.punishment.appeal;

import com.seiama.sentinel.common.discord.Emoji;
import com.seiama.sentinel.common.discord.Modals;
import com.seiama.sentinel.common.model.AppealModel;
import com.seiama.sentinel.common.model.AppealRepository;
import com.seiama.sentinel.common.model.PunishmentRepository;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.spec.InteractionReplyEditMono;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

class VoteButtonHandler implements Function<ButtonInteractionEvent, Publisher<Object>> {
  private static final BiFunction<InteractionReplyEditMono, AppealModel.Complete, Mono<?>> VOTE_BUTTON_REFRESHER = (edit, model) -> {
    return edit.withComponents(Appeals.createVoteButtons(model.votes()));
  };
  private final PunishmentRepository punishments;
  private final AppealRepository appeals;

  VoteButtonHandler(final PunishmentRepository punishments, final AppealRepository appeals) {
    this.punishments = punishments;
    this.appeals = appeals;
  }

  @Override
  public Publisher<Object> apply(final ButtonInteractionEvent event) {
    final AppealModel.Vote vote = Appeals.VOTE_BUTTONS.get(event.getCustomId());
    if (vote != null) {
      final Snowflake user = event.getInteraction().getUser().getId();
      final Snowflake channelId = event.getInteraction().getChannelId();

      return this.appeals.findByAppealDiscussionThreadAndResultIsNull(channelId)
        .zipWhen(appeal -> this.punishments.findById(appeal.punishment()))
        .flatMap(TupleUtils.function((appeal, punishment) -> {
          if (!vote.canVoteWithIfPunisher() && user.equals(punishment.punisherId())) {
            return event.deferEdit()
              .then(VOTE_BUTTON_REFRESHER.apply(event.editReply(), appeal))
              .then(
                event.createFollowup()
                  .withEphemeral(true)
                  .withContent("%s You can't cast \"%s\" on this vote as you are the one who created this punishment.".formatted(
                    Emoji.NO.asFormat(),
                    vote.strings().name()
                  ))
              );
          }
          final BiFunction<InteractionReplyEditMono, String, Mono<Void>> updateAndRefresh = (edit, reason) -> {
            return this.appeals.update(appeal._id(), AppealModel.setVote(user, vote, reason))
              .flatMap(newModel -> VOTE_BUTTON_REFRESHER.apply(edit, newModel))
              .then();
          };
          if (vote.requiresReason()) {
            return Modals.presentAndCaptureSingleTextInput(event, "Veto Vote", "Reason", true, (modal, reason) -> {
              return modal.deferEdit()
                .then(updateAndRefresh.apply(modal.editReply(), reason.orElse(null)));
            }, timeout -> {
              return event.createFollowup("You must provide a reason when submitting a veto vote.")
                .withEphemeral(true)
                .then(Mono.empty());
            });
          } else {
            return event.deferEdit()
              .then(updateAndRefresh.apply(event.editReply(), null));
          }
        }));
    }
    return Mono.empty();
  }
}
