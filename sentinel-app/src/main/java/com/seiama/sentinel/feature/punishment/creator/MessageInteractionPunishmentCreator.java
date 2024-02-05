package com.seiama.sentinel.feature.punishment.creator;

import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.feature.punishment.PunishmentAction;
import com.seiama.sentinel.feature.punishment.Punishments;
import com.seiama.sentinel.feature.punishment.display.PunishmentMessages;
import com.seiama.sentinel.feature.punishment.predicate.CanPunish;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import java.time.Instant;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

@NullMarked
public final class MessageInteractionPunishmentCreator implements Punishments.Creator {
  private final GatewayDiscordClient client;
  private final DeferrableInteractionEvent event;
  private final Message message;
  private final Guild guild;
  private final PunishmentModel.Type type;
  private final PunishmentAction<User, PunishmentModel.Complete> action;
  private final @Nullable String reason;

  public MessageInteractionPunishmentCreator(final GatewayDiscordClient client, final DeferrableInteractionEvent event, final Message message, final Guild guild, final PunishmentModel.Type type, final PunishmentAction<User, PunishmentModel.Complete> action, final @Nullable String reason) {
    this.client = client;
    this.event = event;
    this.message = message;
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
    return Mono.justOrEmpty(this.message.getAuthor())
      .filterWhen(new CanPunish<>(guilds, this.guild, punisher))
      .switchIfEmpty(
        this.event.editReply()
          .withContentOrNull(PunishmentMessages.mayNotPunish())
          .then(Mono.empty())
      )
      .flatMap(punished -> punishments.create(
        this.client,
        this.guild,
        PunishmentModel.Complete.create(
          this.guild.getId(),
          this.type,
          Instant.now(),
          Optional.of(punisher),
          punished,
          this.reason,
          null,
          false
        ),
        punished,
        this.action
      ))
      .flatMap(punishment -> {
        return this.event.editReply().withContentOrNull(PunishmentMessages.punishmentPunisherResponse(punishment)).thenReturn(punishment);
      });
  }
}
