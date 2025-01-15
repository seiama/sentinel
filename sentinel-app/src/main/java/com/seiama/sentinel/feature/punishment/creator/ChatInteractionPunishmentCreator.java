package com.seiama.sentinel.feature.punishment.creator;

import com.seiama.sentinel.command.OptionNames;
import com.seiama.sentinel.command.Options;
import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.feature.punishment.PunishmentAction;
import com.seiama.sentinel.feature.punishment.Punishments;
import com.seiama.sentinel.feature.punishment.display.PunishmentMessages;
import com.seiama.sentinel.feature.punishment.predicate.CanPunish;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

@NullMarked
public final class ChatInteractionPunishmentCreator implements Punishments.Creator {
  private final GatewayDiscordClient client;
  private final ChatInputInteractionEvent event;
  private final Guild guild;
  private final PunishmentModel.Type type;
  private final @Nullable Duration duration;
  private final PunishmentAction<User, PunishmentModel.Complete> action;

  public ChatInteractionPunishmentCreator(final GatewayDiscordClient client, final ChatInputInteractionEvent event, final Guild guild, final PunishmentModel.Type type, final @Nullable Duration duration, final PunishmentAction<User, PunishmentModel.Complete> action) {
    this.client = client;
    this.event = event;
    this.guild = guild;
    this.type = type;
    this.duration = duration;
    this.action = action;
  }

  @Override
  public Mono<PunishmentModel.Complete> create(final GuildRepository guilds, final Punishments punishments) {
    final Mono<PunishmentModel.Complete> deferred = Mono.defer(() -> this.deferred(guilds, punishments));
    return this.event.deferReply().then(deferred);
  }

  private Mono<PunishmentModel.Complete> deferred(final GuildRepository guilds, final Punishments punishments) {
    final Interaction interaction = this.event.getInteraction();
    final Member punisher = interaction.getMember().orElseThrow();
    final @Nullable String reason = Options.string(this.event, OptionNames.REASON).orElse(null);
    return Options.user(this.event, OptionNames.MEMBER)
      .orElse(Mono.empty())
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
          reason,
          this.duration,
          false
        ),
        punished,
        this.action
      ))
      .flatMap(punishment -> this.event.editReply().withContentOrNull(PunishmentMessages.punishmentPunisherResponse(punishment)).thenReturn(punishment));
  }
}
