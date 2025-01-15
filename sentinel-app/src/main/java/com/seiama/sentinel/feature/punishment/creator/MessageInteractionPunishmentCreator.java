package com.seiama.sentinel.feature.punishment.creator;

import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.feature.punishment.PunishmentAction;
import com.seiama.sentinel.feature.punishment.Punishments;
import com.seiama.sentinel.feature.punishment.display.PunishmentMessages;
import com.seiama.sentinel.feature.punishment.predicate.CanPunish;
import discord4j.core.event.domain.interaction.MessageInteractionEvent;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import java.time.Instant;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

public final class MessageInteractionPunishmentCreator implements Punishments.Creator {
  private final MessageInteractionEvent event;
  private final Guild guild;
  private final PunishmentModel.Type type;
  private final PunishmentAction<User, PunishmentModel.Complete> action;
  private final @Nullable String reason;

  public MessageInteractionPunishmentCreator(final MessageInteractionEvent event, final Guild guild, final PunishmentModel.Type type, final PunishmentAction<User, PunishmentModel.Complete> action, final @Nullable String reason) {
    this.event = event;
    this.guild = guild;
    this.type = type;
    this.action = action;
    this.reason = reason;
  }

  @Override
  public Mono<PunishmentModel.Complete> create(final GuildRepository guilds, final Punishments punishments) {
    final Mono<PunishmentModel.Complete> deferred = Mono.defer(() -> this.deferred(guilds, punishments));
    return this.event.deferReply().then(deferred);
  }

  private Mono<PunishmentModel.Complete> deferred(final GuildRepository guilds, final Punishments punishments) {
    final Interaction interaction = this.event.getInteraction();
    final Member punisher = interaction.getMember().orElseThrow();
    return Mono.justOrEmpty(this.event.getResolvedMessage().getAuthor())
      .filterWhen(new CanPunish<>(guilds, this.guild, punisher))
      .switchIfEmpty(
        this.event.editReply()
          .withContentOrNull(PunishmentMessages.mayNotPunish())
          .then(Mono.empty())
      )
      .flatMap(punished -> punishments.create(
        this.guild,
        PunishmentModel.Complete.create(
          this.guild.getId(),
          this.type,
          Instant.now(),
          punisher,
          punished,
          this.reason,
          false
        ),
        punished,
        this.action
      ))
      .flatMap(punishment -> this.event.editReply().withContentOrNull(PunishmentMessages.punishmentPunisherResponse(punishment)).thenReturn(punishment));
  }
}
