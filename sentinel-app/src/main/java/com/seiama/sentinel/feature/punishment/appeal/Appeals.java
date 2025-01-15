package com.seiama.sentinel.feature.punishment.appeal;

import com.seiama.common.Comparables;
import com.seiama.sentinel.common.Listener;
import com.seiama.sentinel.common.discord.Discord;
import com.seiama.sentinel.common.discord.Emoji;
import com.seiama.sentinel.common.discord.UserDisplay;
import com.seiama.sentinel.common.model.AppealModel;
import com.seiama.sentinel.common.model.AppealRepository;
import com.seiama.sentinel.common.model.GuildModel;
import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.common.model.PunishmentRepository;
import com.seiama.sentinel.common.model.UserIdentity;
import com.seiama.sentinel.feature.punishment.Punishments;
import com.seiama.sentinel.model.TemporaryMessageLinkRepository;
import com.seiama.sentinel.reactive.Reactive;
import discord4j.common.util.Snowflake;
import discord4j.common.util.TimestampFormat;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.event.domain.message.MessageUpdateEvent;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.util.MentionUtil;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.MessageEditRequest;
import discord4j.discordjson.json.PermissionsEditRequest;
import discord4j.discordjson.json.ThreadModifyRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.RestClient;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.function.Function3;
import reactor.util.function.Tuple2;

@Component
@NullMarked
@SuppressWarnings("FinalClass")
public class Appeals implements Listener {
  static final int CHANNEL_RATE_LIMIT = 5; // in seconds
  static final int THREAD_AUTO_ARCHIVE_DURATION = 10080; // 7 days, in minutes

  static final int NEW_APPEAL_NOTIFICATION_COLOR = 0x0089b4;

  static final String NO_APPEAL_ASSOCIATED_WITH_THIS_CHANNEL = "There is no appeal associated with this channel.";
  static final String NO_ACTIVE_APPEAL = "The appeal associated with this channel is no longer active.";

  private static final Duration COOLDOWN_NO = Duration.ofDays(30 * 6); // 6 months, approximately
  private static final Duration COOLDOWN_LATER = Duration.ofDays(30); // 1 month, approximately
  private static final Duration COOLDOWN_VETO = Duration.ofDays(30 * 6); // 6 months, approximately

  static final int MINIMUM_VOTES = 3;
  private static final Duration VOTE_DURATION = Duration.ofDays(3);
  private static final Duration VOTE_CHECK_INTERVAL = Duration.ofMinutes(30);

  private static final String VOTE_BUTTON_PREFIX = "appeal-vote:";
  static final Map<String, AppealModel.Vote> VOTE_BUTTONS = AppealModel.Vote.all()
    .collect(Collectors.toMap(Appeals::getVoteButtonId, Function.identity()));
  private static final Map<AppealModel.Vote, Function3<String, ReactionEmoji, String, Button>> VOTE_BUTTON_FACTORY = Map.of(
    AppealModel.Vote.YES, Button::success,
    AppealModel.Vote.NO, Button::danger,
    AppealModel.Vote.ABSTAIN, Button::primary,
    AppealModel.Vote.LATER, Button::secondary,
    AppealModel.Vote.VETO, Button::danger
  );

  private final GuildRepository guilds;
  private final PunishmentRepository punishments;
  private final Punishments punishmentOps;
  private final AppealRepository appeals;
  private final TemporaryMessageLinkRepository messageLinks;
  private final RestClient relayRest;

  @Autowired
  private Appeals(final GuildRepository guilds, final PunishmentRepository punishments, final Punishments punishmentOps, final AppealRepository appeals, final TemporaryMessageLinkRepository messageLinks, final @Qualifier("relayRest") RestClient relayRest) {
    this.guilds = guilds;
    this.punishments = punishments;
    this.punishmentOps = punishmentOps;
    this.appeals = appeals;
    this.messageLinks = messageLinks;
    this.relayRest = relayRest;
  }

  @Override
  public Mono<Void> listen(final GatewayDiscordClient client) {
    final Flux<Void> voteTicker = Flux.interval(Duration.ofMinutes(1), VOTE_CHECK_INTERVAL, Schedulers.newSingle("Punishment Appeal Vote Result Ticker"))
      .flatMap(tick -> {
        return this.appeals.findAllByResultIsNull()
          .filter(model -> Comparables.greaterThanOrEqual(Duration.between(model.date(), Instant.now()), VOTE_DURATION))
          .flatMap(model -> {
            final VoteFinisher finisher = new VoteFinisher(client, model);
            return finisher.create();
          });
      });
    return Mono.when(
      voteTicker,
      client.on(MemberJoinEvent.class, new StartHandler(client, this.guilds, this.punishmentOps, this.appeals, this.relayRest)),
      client.on(MemberLeaveEvent.class, event -> {
        final User user = event.getUser();
        return this.appeals.findByGuildAndUserAndResultIsNull(event.getGuildId(), user.getId())
          .flatMap(model -> this.cancel(client, model, user));
      }),
      client.on(MessageCreateEvent.class, new MessageLinkCreateHandler(this.guilds, this.appeals, this.messageLinks, this.relayRest)),
      client.on(MessageUpdateEvent.class, new MessageLinkUpdateHandler(this.guilds, this.messageLinks, this.relayRest)),
      client.on(MessageDeleteEvent.class, new MessageLinkDeleteHandler(this.messageLinks, this.relayRest)),
      client.on(ButtonInteractionEvent.class, new VoteButtonHandler(this.punishments, this.appeals))
    );
  }

  static String createChannelName(final Member member, final ObjectId appealId) {
    return "a-" + member.getId().asLong() + "-" + appealId;
  }

  static String createThreadName(final Member member) {
    return UserDisplay.render(UserDisplay.Renderer.USERNAME, new UserIdentity(member));
  }

  Mono<AppealModel.Complete> findByAppealThread(final Snowflake channel) {
    return this.appeals.findByAppealThread(channel);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  static EmbedData createMessage(final MessageData message, final Optional<User> author, final Tuple2<Guild, GuildModel.Complete> guild) {
    return EmbedData.builder()
      .description(message.content())
      .timestamp(message.editedTimestamp().orElse(message.timestamp()))
      .author(
        guild.getT1().getId().equals(guild.getT2().guild())
          ? Discord.author(guild.getT1()).map(EmbedCreateFields.Author::asRequest).map(Possible::of).orElse(Possible.absent())
          : Possible.of(Discord.author(message.author(), author))
      )
      .build();
  }

  static String getVoteButtonId(final AppealModel.Vote vote) {
    return VOTE_BUTTON_PREFIX + vote.strings().button();
  }

  static ActionRow createVoteButtons(final Map<String, List<Snowflake>> votes) {
    return ActionRow.of(
      AppealModel.Vote.all()
        .map(vote -> VOTE_BUTTON_FACTORY.get(vote).apply(
          getVoteButtonId(vote),
          vote.emoji(),
          String.format("%s (%d)", vote.strings().name(), votes.getOrDefault(vote.name(), List.of()).size())
        ))
        .toList()
    );
  }

  static List<EmbedCreateSpec> createVoteSummary(final Map<String, List<Snowflake>> votes) {
    final StringBuilder description = new StringBuilder();
    AppealModel.Vote.all()
      .forEach(vote -> {
        description.append("- %s %s: %s%s".formatted(
          vote.emoji().asFormat(),
          vote.strings().name(),
          votes.getOrDefault(vote.name(), List.of()).stream()
            .map(MentionUtil::forUser)
            .collect(Collectors.joining(", ")),
          vote != AppealModel.Vote.VETO ? "\n" : ""
        ));
      });
    return List.of(
      EmbedCreateSpec.builder()
        .title("Voting")
        .description("Please cast your vote using one of the buttons below. If you wish to change your vote, simply click a different button.")
        .build(),
      EmbedCreateSpec.builder()
        .title("Vote overview")
        .description(description.toString())
        .color(Color.of(0xcabd85))
        .build()
    );
  }

  class VoteFinisher {
    final GatewayDiscordClient client;
    final AppealModel.Complete model;

    VoteFinisher(final GatewayDiscordClient client, final AppealModel.Complete model) {
      this.client = client;
      this.model = model;
    }

    Mono<Void> create() {
      final AppealModel.VoteResult result = VoteResultFinder.resultOf(
        this.model.votes().entrySet()
          .stream()
          .collect(Collectors.toMap(entry -> AppealModel.Vote.valueOf(entry.getKey()), Map.Entry::getValue))
      );
      return Mono.when(
        this.client.getSelf().flatMap(user -> {
          final Instant now = Instant.now();
          return switch (result) {
            case NONE -> this.client.rest().getChannelById(this.model.appealDiscussionThread()).createMessage(String.format(
              "%s The result of the vote could not be determined at this time and will be recalculated %s.",
              Emoji.CLOCK1.asFormat(),
              TimestampFormat.LONG_DATE_TIME.format(Instant.now().plus(VOTE_CHECK_INTERVAL))
            ));
            case YES -> Appeals.this.accept(this.client, this.model, user);
            case NO -> Appeals.this.deny(this.client, this.model, user, null, now.plus(COOLDOWN_NO));
            case LATER -> Appeals.this.deny(this.client, this.model, user, null, now.plus(COOLDOWN_LATER));
            case VETO -> Appeals.this.deny(this.client, this.model, user, null, now.plus(COOLDOWN_VETO));
          };
        })
      );
    }
  }

  Mono<Void> accept(final GatewayDiscordClient client, final AppealModel.Complete model, final User user) {
    return this.acceptOrDenyOrCancel(new AppealFinisher(client, model, user, AppealModel.Result.ACCEPTED, null, null));
  }

  Mono<Void> deny(final GatewayDiscordClient client, final AppealModel.Complete model, final User user, final @Nullable String reason, final @Nullable Instant nextAttemptMayBeMadeAt) {
    return this.acceptOrDenyOrCancel(new AppealFinisher(client, model, user, AppealModel.Result.DENIED, reason, nextAttemptMayBeMadeAt));
  }

  private Mono<Void> cancel(final GatewayDiscordClient client, final AppealModel.Complete model, final User user) {
    return this.acceptOrDenyOrCancel(new AppealFinisher(client, model, user, AppealModel.Result.CANCELLED, null, null));
  }

  private Mono<Void> acceptOrDenyOrCancel(final AppealFinisher finisher) {
    return finisher.create();
  }

  class AppealFinisher {
    final GatewayDiscordClient client;
    final AppealModel.Complete model;
    final User user; // (accepted, denied) -> (staff | bot) | (cancelled) -> punished
    final boolean automatic;
    final AppealModel.Result result;
    final @Nullable String reason;
    final @Nullable Instant nextAttemptMayBeMadeAt;

    AppealFinisher(
      final GatewayDiscordClient client,
      final AppealModel.Complete model,
      final User user,
      final AppealModel.Result result,
      final @Nullable String reason,
      final @Nullable Instant nextAttemptMayBeMadeAt
    ) {
      this.client = client;
      this.model = model;
      this.user = user;
      this.automatic = user.getId().equals(client.getSelfId());
      this.result = result;
      this.reason = reason;
      this.nextAttemptMayBeMadeAt = resolveNextAttemptMayBeMadeAt(nextAttemptMayBeMadeAt, result);
    }

    private static @Nullable Instant resolveNextAttemptMayBeMadeAt(final @Nullable Instant instant, final AppealModel.Result result) {
      if (instant != null) {
        return instant;
      }
      if (result == AppealModel.Result.DENIED) {
        return Instant.now().plus(COOLDOWN_NO);
      }
      return null;
    }

    Mono<Void> create() {
      return Mono.when(
        Appeals.this.appeals.update(this.model, new AppealModel.Partial.Close() {
          @Override
          public AppealModel.Result result() {
            return AppealFinisher.this.result;
          }

          @Override
          public @Nullable String reason() {
            return AppealFinisher.this.reason;
          }

          @Override
          public @Nullable Instant nextAttemptMayBeMadeAt() {
            return AppealFinisher.this.nextAttemptMayBeMadeAt;
          }
        }),
        this.unenforce(),
        this.sendMessagesToChannelsAndThreadsAndThenArchiveAndClose(),
        Appeals.this.messageLinks.deleteAllByTargetChannelId(this.model.appealChannel().asLong()).onErrorResume(Reactive.ignoringException()),
        Appeals.this.messageLinks.deleteAllByTargetChannelId(this.model.appealThread().asLong()).onErrorResume(Reactive.ignoringException())
      );
    }

    private Mono<Void> unenforce() {
      if (this.result == AppealModel.Result.ACCEPTED) {
        final Mono<PunishmentModel.Complete> updatedPunishment = Appeals.this.punishments.update(this.model.punishment(), PunishmentModel.Partial.Stale.of(Optional.of(this.user), this.reason, this.automatic, this.model._id()));
        return updatedPunishment
          .flatMap(punishment -> {
            return this.client.getGuildById(punishment.guild())
              .flatMap(guild -> Appeals.this.punishmentOps.unenforce(guild, punishment, String.format(
                "Punishment (%s) has been appealed (%s).",
                punishment._id(),
                this.model._id()
              )));
          });
      }
      return Mono.empty();
    }

    private Mono<Void> sendMessagesToChannelsAndThreadsAndThenArchiveAndClose() {
      final DiscordClient rest = this.client.rest();
      final UnaryOperator<EmbedCreateSpec.Builder> embedForBoth = embed -> {
        return embed
          .color(Color.of(this.result.color()))
          .title("Appeal " + this.result.strings().name());
      };
      final EmbedData embedForPunished = this.createEmbedForPunished(embedForBoth.apply(EmbedCreateSpec.builder())).asRequest();
      final EmbedData embedForStaff = this.createEmbedForStaff(embedForBoth.apply(EmbedCreateSpec.builder())).asRequest();
      final String reasonForActionLog = "Appeal has been " + this.result.strings().name();
      return Appeals.this.guilds.findByGuild(this.model.guild())
        .flatMap(guildModel -> Mono.when(
          Mono.when(
            Mono.just(this.result)
              .filter(result -> result != AppealModel.Result.CANCELLED)
              .flatMap(result -> rest.getChannelById(this.model.appealChannel()).createMessage(embedForPunished)),
            Mono.just(this.result)
              .filter(result -> result == AppealModel.Result.ACCEPTED)
              .flatMap(accepted -> rest.getChannelById(this.model.appealChannel()).createMessage(guildModel.invite())),
            Mono.justOrEmpty(this.model.voteMessage()).flatMap(voteMessage -> rest.getMessageById(this.model.appealDiscussionThread(), voteMessage).edit(
              MessageEditRequest.builder()
                .components(Possible.of(Optional.empty()))
                .build()
            ))
          ).then(
            rest.getChannelById(this.model.appealChannel()).editChannelPermissions(
              this.model.user(),
              PermissionsEditRequest.builder()
                .type(PermissionOverwrite.Type.MEMBER.getValue())
                .allow(PermissionSet.of(Permission.VIEW_CHANNEL).getRawValue())
                .deny(PermissionSet.of(Permission.SEND_MESSAGES).getRawValue())
                .build(),
              reasonForActionLog
            ).onErrorResume(ClientException.class, Reactive.<Void>ignoringException()) // avoid possible 10009 if the user has left the guild
          ),
          rest.getChannelById(this.model.appealThread()).createMessage(embedForStaff).then(
            rest.getChannelService().modifyThread(
              this.model.appealThread().asLong(),
              ThreadModifyRequest.builder()
                .archived(true)
                .locked(true)
                .build(),
              reasonForActionLog
            )
          ),
          rest.getChannelById(this.model.appealDiscussionThread()).createMessage(embedForStaff).then(
            rest.getChannelService().modifyThread(
              this.model.appealDiscussionThread().asLong(),
              ThreadModifyRequest.builder()
                .archived(true)
                .locked(true)
                .build(),
              reasonForActionLog
            )
          )
        ));
    }

    private EmbedCreateSpec createEmbedForPunished(final EmbedCreateSpec.Builder embed) {
      embed.description(switch (this.result) {
        case ACCEPTED -> "Your appeal has been accepted. Please be sure you are up to date with the rules in our community prior to re-joining.";
        case DENIED -> "Your appeal has been denied at this time." + this.nextAttemptMayBeMadeAt("Your");
        case CANCELLED -> "Your appeal has been cancelled. You may make another attempt at the appeal process if you wish.";
      });
      if (this.reason != null) {
        embed.addField("Reason", this.reason, false);
      }
      return embed.build();
    }

    private EmbedCreateSpec createEmbedForStaff(final EmbedCreateSpec.Builder embed) {
      embed.description(switch (this.result) {
        case ACCEPTED -> "The appeal was accepted, and the user has been provided with information about re-joining the community.";
        case DENIED -> "The appeal was denied." + this.nextAttemptMayBeMadeAt("The");
        case CANCELLED -> "The appeal was cancelled.";
      });
      embed.addField(this.result.strings().nameForStartOfSentence() + " by", UserDisplay.render(UserDisplay.Renderer.MENTION_WITH_TRAILING_BACKTICK_WRAPPED_USERNAME_AND_ID, new UserIdentity(this.user)), false);
      if (this.reason != null) {
        embed.addField("Reason", this.reason, false);
      }
      if (this.automatic) {
        embed.addField("Automatic Decision", Emoji.YES.asFormat(), false);
      }
      return embed.build();
    }

    private String nextAttemptMayBeMadeAt(final String start) {
      if (this.nextAttemptMayBeMadeAt != null) {
        return String.format(" %s next attempt at an appeal may be made %s.", start, TimestampFormat.LONG_DATE_TIME.format(this.nextAttemptMayBeMadeAt));
      }
      return "";
    }
  }
}
