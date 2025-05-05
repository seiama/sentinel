package com.seiama.sentinel.feature.punishment.creator;

import com.seiama.sentinel.common.discord.Messages;
import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.common.model.UserIdentity;
import com.seiama.sentinel.feature.punishment.PunishmentAction;
import com.seiama.sentinel.feature.punishment.Punishments;
import com.seiama.sentinel.feature.punishment.display.PunishmentDisplay;
import com.seiama.sentinel.feature.punishment.display.PunishmentDisplayStyle;
import com.seiama.sentinel.feature.punishment.display.PunishmentMessages;
import com.seiama.sentinel.feature.punishment.predicate.CanPunish;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.DeferrableInteractionEvent;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.rest.util.AllowedMentions;
import java.time.Duration;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

@NullMarked
public abstract class AbstractInteractionPunishmentCreator<E extends DeferrableInteractionEvent> implements Punishments.Creator {
  protected final GatewayDiscordClient client;
  protected final E event;
  protected final Guild guild;
  protected final PunishmentModel.Type type;
  protected final @Nullable Duration duration;
  protected final PunishmentAction<User, PunishmentModel.Complete> action;

  public AbstractInteractionPunishmentCreator(
    final GatewayDiscordClient client,
    final E event,
    final Guild guild,
    final PunishmentModel.Type type,
    final @Nullable Duration duration,
    final PunishmentAction<User, PunishmentModel.Complete> action
  ) {
    this.client = client;
    this.event = event;
    this.guild = guild;
    this.type = type;
    this.duration = duration;
    this.action = action;
  }

  @Override
  public final Mono<PunishmentModel.Complete> create(final GuildRepository guilds, final Punishments punishments) {
    final Mono<PunishmentModel.Complete> deferred = Mono.defer(() -> this.deferred(guilds, punishments));
    return this.event.deferReply().then(deferred);
  }

  protected Mono<PunishmentModel.Complete> deferred(final GuildRepository guilds, final Punishments punishments) {
    final Interaction interaction = this.event.getInteraction();
    final Member punisher = interaction.getMember().orElseThrow();
    return this.getPunishedUser()
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
          Messages.getTimestampOrNow(interaction.getMessage()),
          Optional.of(new UserIdentity(punisher)),
          new UserIdentity(punished),
          this.getReason(),
          this.duration,
          false
        ),
        punished,
        this.action
      ))
      .flatMap(punishment -> this.event.editReply()
        .withAllowedMentionsOrNull(AllowedMentions.suppressAll())
        .withComponents(PunishmentDisplay.punishment(punishment, PunishmentDisplayStyle.CREATED))
        .thenReturn(punishment)
      );
  }

  protected abstract Mono<User> getPunishedUser();

  protected abstract @Nullable String getReason();
}
