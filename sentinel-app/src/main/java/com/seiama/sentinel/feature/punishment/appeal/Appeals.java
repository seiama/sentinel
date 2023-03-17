package com.seiama.sentinel.feature.punishment.appeal;

import com.seiama.common.Comparables;
import com.seiama.sentinel.common.Listener;
import com.seiama.sentinel.common.discord.Discord;
import com.seiama.sentinel.common.discord.Emoji;
import com.seiama.sentinel.common.discord.Mention;
import com.seiama.sentinel.common.model.AppealModel;
import com.seiama.sentinel.common.model.AppealRepository;
import com.seiama.sentinel.common.model.Feature;
import com.seiama.sentinel.common.model.GuildModel;
import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.common.model.PunishmentRepository;
import com.seiama.sentinel.feature.punishment.display.PunishmentDisplay;
import com.seiama.sentinel.feature.punishment.display.PunishmentDisplayStyle;
import com.seiama.sentinel.model.TemporaryMessageLink;
import com.seiama.sentinel.model.TemporaryMessageLinkRepository;
import com.seiama.sentinel.reactive.Reactive;
import discord4j.common.util.Snowflake;
import discord4j.common.util.TimestampFormat;
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
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.EmbedData;
import discord4j.discordjson.json.MessageData;
import discord4j.discordjson.json.MessageEditRequest;
import discord4j.discordjson.json.PermissionsEditRequest;
import discord4j.discordjson.json.StartThreadWithoutMessageRequest;
import discord4j.discordjson.json.ThreadModifyRequest;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.entity.RestChannel;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.function.Function3;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

@Component
@SuppressWarnings("FinalClass")
public class Appeals implements Listener {
  private static final int CHANNEL_RATE_LIMIT = 5; // in seconds
  private static final int THREAD_AUTO_ARCHIVE_DURATION = 10080; // 7 days, in minutes

  private static final int NEW_APPEAL_NOTIFICATION_COLOR = 0x0089b4;

  static final String NO_APPEAL_ASSOCIATED_WITH_THIS_CHANNEL = "There is no appeal associated with this channel.";

  private static final Duration COOLDOWN_NO = Duration.ofDays(30 * 6); // 6 months, approximately
  private static final Duration COOLDOWN_LATER = Duration.ofDays(30); // 1 month, approximately
  private static final Duration COOLDOWN_VETO = Duration.ofDays(30 * 6); // 6 months, approximately

  static final int MINIMUM_VOTES = 3;
  private static final Duration VOTE_DURATION = Duration.ofDays(3);
  private static final Duration VOTE_CHECK_INTERVAL = Duration.ofMinutes(30);

  private static final AppealModel.Vote[] VOTES = AppealModel.Vote.values();
  private static final String VOTE_BUTTON_PREFIX = "appeal-vote:";
  private static final Map<String, AppealModel.Vote> VOTE_BUTTONS = Arrays.stream(VOTES)
    .collect(Collectors.toMap(vote -> VOTE_BUTTON_PREFIX + vote.words().button(), Function.identity()));
  private static final Map<AppealModel.Vote, Function3<String, ReactionEmoji, String, Button>> VOTE_BUTTON_FACTORY = Map.of(
    AppealModel.Vote.YES, Button::success,
    AppealModel.Vote.NO, Button::danger,
    AppealModel.Vote.ABSTAIN, Button::primary,
    AppealModel.Vote.LATER, Button::secondary,
    AppealModel.Vote.VETO, Button::danger
  );

  private final GuildRepository guilds;
  private final PunishmentRepository punishments;
  private final AppealRepository appeals;
  private final TemporaryMessageLinkRepository messageLinks;

  @Autowired
  private Appeals(final GuildRepository guilds, final PunishmentRepository punishments, final AppealRepository appeals, final TemporaryMessageLinkRepository messageLinks) {
    this.guilds = guilds;
    this.punishments = punishments;
    this.appeals = appeals;
    this.messageLinks = messageLinks;
  }

  @Override
  public @NotNull Mono<Void> listen(final @NotNull GatewayDiscordClient client) {
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
      client.on(MemberJoinEvent.class, event -> {
        return this.guilds.findByFeaturesPunishmentsAppealsGuild(event.getGuildId())
          .filter(Feature.PUNISHMENTS_APPEALS.enabledForGuild())
          .flatMap(guildModel -> {
            final GuildModel.Complete.Features.Punishments.Appeals config = guildModel.features().punishments().appeals();
            final Member member = event.getMember();
            final Mono<PunishmentModel.Complete> getPunishment = this.punishments.findByPunishedIdAndStaleIsNotOrderByDateDesc(member.getId(), true);
            final Mono<PunishmentModel.Complete> kickUserForNoActivePunishment = event.getGuild()
              .flatMap(guild -> guild.kick(member.getId(), null))
              .then(Mono.empty());
            final Mono<TextChannel> createAppealChannel = event.getGuild()
              .flatMap(guild -> guild.createTextChannel(createChannelName(member))
                .withParentId(config.appealChannelsCategory())
                .withPermissionOverwrites(
                  PermissionOverwrite.forMember(member.getId(), PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none()),
                  PermissionOverwrite.forRole(config.everyoneRole(), PermissionSet.none(), PermissionSet.of(Permission.VIEW_CHANNEL, Permission.ATTACH_FILES))
                )
                .withRateLimitPerUser(CHANNEL_RATE_LIMIT)
              );
            final Mono<ChannelData> createAppealThread = client.rest().getChannelService().startThreadWithoutMessage(
              config.appealThreadsChannel().asLong(),
              StartThreadWithoutMessageRequest.builder()
                .type(Channel.Type.GUILD_PRIVATE_THREAD.getValue())
                .name(createThreadName(member))
                .autoArchiveDuration(THREAD_AUTO_ARCHIVE_DURATION)
                .build()
            );
            final Mono<ChannelData> createAppealDiscussionThread = client.rest().getChannelService().startThreadWithoutMessage(
              config.appealDiscussionThreadsChannel().asLong(),
              StartThreadWithoutMessageRequest.builder()
                .type(Channel.Type.GUILD_PRIVATE_THREAD.getValue())
                .name(createThreadName(member))
                .autoArchiveDuration(THREAD_AUTO_ARCHIVE_DURATION)
                .build()
            );
            return Reactive.zipSequence(
                getPunishment
                  .switchIfEmpty(kickUserForNoActivePunishment),
                createAppealChannel,
                createAppealThread,
                createAppealDiscussionThread
              )
              .zipWhen(TupleUtils.function((punishment, channel, appealThread, appealDiscussionThread) -> this.appeals.insert(new AppealModel.Complete(
                new ObjectId(),
                punishment.guild(),
                Instant.now(),
                member.getId(),
                punishment._id(),
                channel.getId(),
                Snowflake.of(appealThread.id()),
                Snowflake.of(appealDiscussionThread.id()),
                null, // we haven't created the message yet
                Map.of(),
                null,
                null,
                null
              ))))
              .map(tuple -> {
                final Tuple4<PunishmentModel.Complete, TextChannel, ChannelData, ChannelData> t1 = tuple.getT1();
                return Tuples.of(t1.getT1(), t1.getT2(), t1.getT3(), t1.getT4(), tuple.getT2());
              })
              .flatMap(TupleUtils.function((punishment, channel, appealThread, appealDiscussionThread, model) -> {
                final RestChannel appealThreadChannel = client.rest().getChannelById(Snowflake.of(appealThread.id()));
                final RestChannel appealDiscussionThreadChannel = client.rest().getChannelById(Snowflake.of(appealDiscussionThread.id()));
                return Mono.when(
                  channel.edit().withTopic(model._id().toString()),
                  channel.createMessage(PunishmentDisplay.punishment(punishment, PunishmentDisplayStyle.APPEAL)),
                  channel.createMessage(String.format("Hey, %s! This appeal is now active. Please explain why you think this punishment should be appealed.", member.getMention())),
                  appealThreadChannel.createMessage(PunishmentDisplay.punishment(punishment, PunishmentDisplayStyle.FULL).asRequest()),
                  appealThreadChannel.createMessage(String.format(
                    "%s Please note that any messages sent in this channel will be shared with the user who is appealing. For staff discussion, please use %s",
                    Emoji.toString(Emoji.WARNING),
                    Mention.channel(appealDiscussionThread.id().asLong())
                  )),
                  appealDiscussionThreadChannel.createMessage(
                    MessageCreateSpec.builder()
                      .addEmbed(
                        EmbedCreateSpec.builder()
                          .title("Voting")
                          .description("Please cast your vote using one of the buttons below. If you wish to change your vote, simply click a different button.")
                          .build()
                      )
                      .addComponent(createVoteButtons(Map.of()))
                      .build()
                      .asRequest()
                  ).flatMap(voteMessage -> Mono.when(
                    client.rest().getChannelService().addPinnedMessage(appealDiscussionThread.id().asLong(), voteMessage.id().asLong()),
                    this.appeals.update(model._id(), (AppealModel.Partial.VoteMessage) () -> Snowflake.of(voteMessage.id()))
                  )),
                  client.rest().getChannelById(config.appealThreadsChannel()).createMessage(
                    EmbedCreateSpec.builder()
                      .color(Color.of(NEW_APPEAL_NOTIFICATION_COLOR))
                      .title("A new appeal has been created")
                      .description(String.format("A new appeal has been created by %s.", Mention.userWithId(member.getId(), member.getUsername(), member.getDiscriminator())))
                      .addField("Appeal channel", Mention.channel(appealThread.id().asLong()), false)
                      .addField("Discussion channel", Mention.channel(appealDiscussionThread.id().asLong()), false)
                      .build()
                      .asRequest()
                  )
                );
              }));
          });
      }),
      client.on(MemberLeaveEvent.class, event -> {
        final User user = event.getUser();
        return this.appeals.findByGuildAndUserAndResultIsNull(event.getGuildId(), user.getId())
          .flatMap(model -> this.cancel(client, model, user));
      }),
      client.on(MessageCreateEvent.class, event -> {
        final Message message = event.getMessage();
        if (Discord.isBot(message.getAuthor())) {
          return Mono.empty();
        }
        return message.getGuild()
          .zipWhen(guild -> this.guilds.findByGuildOrFeaturesPunishmentsAppealsGuild(guild.getId(), guild.getId()))
          .filter(tuple -> Feature.PUNISHMENTS_APPEALS.enabledForGuild(tuple.getT2()))
          .zipWith(message.getChannel())
          // todo: filter channel by ids
          .flatMap(TupleUtils.function((guild, channel) -> {
            final Channel.Type type = channel.getType();
            if (type == Channel.Type.GUILD_TEXT) {
              return Mono.just(Tuples.<Tuple2<Guild, GuildModel.Complete>, Supplier<Mono<AppealModel.Complete>>, Function<AppealModel.Complete, Snowflake>>of(
                guild,
                () -> this.appeals.findByAppealChannel(channel.getId()),
                AppealModel.Complete::appealThread
              ));
            } else if (type == Channel.Type.GUILD_PRIVATE_THREAD) {
              return Mono.just(Tuples.<Tuple2<Guild, GuildModel.Complete>, Supplier<Mono<AppealModel.Complete>>, Function<AppealModel.Complete, Snowflake>>of(
                guild,
                () -> this.appeals.findByAppealThread(channel.getId()),
                AppealModel.Complete::appealChannel
              ));
            }
            return Mono.empty();
          }))
          .flatMap(TupleUtils.function((guild, model, targetChannelId) -> {
            return model.get()
              .map(targetChannelId)
              .flatMap(channelId -> client.rest().getChannelById(channelId).createMessage(this.createMessage(message.getData(), message.getAuthor(), guild)))
              .flatMap(newMessage -> this.messageLinks.save(new TemporaryMessageLink(
                guild.getT2()._id(),
                message.getChannelId(),
                message.getId(),
                Snowflake.of(newMessage.channelId()),
                Snowflake.of(newMessage.id())
              )));
          }));
      }),
      client.on(MessageUpdateEvent.class, event -> {
        return this.messageLinks.findBySourceMessageId(event.getMessageId().asLong())
          .flatMap(targetIds -> {
            return event.getGuild()
              .zipWith(event.getMessage())
              .flatMap(TupleUtils.function((guild, message) -> {
                return this.guilds.findById(targetIds.guild())
                  .flatMap(guildModel -> {
                    return client.rest().getMessageById(targetIds.targetChannelId(), targetIds.targetMessageId()).edit(
                      MessageEditRequest.builder()
                        .embeds(Possible.of(Optional.of(List.of(
                          this.createMessage(message.getData(), message.getAuthor(), Tuples.of(guild, guildModel))
                        ))))
                        .build()
                    );
                  });
              }));
          });
      }),
      client.on(MessageDeleteEvent.class, event -> {
        return this.messageLinks.findBySourceMessageId(event.getMessageId().asLong())
          .flatMap(targetIds -> {
            return client.rest().getChannelService().deleteMessage(targetIds.targetChannelId().asLong(), targetIds.targetMessageId().asLong(), null);
          });
      }),
      client.on(ButtonInteractionEvent.class, event -> {
        final AppealModel.Vote vote = VOTE_BUTTONS.get(event.getCustomId());
        if (vote != null) {
          final Snowflake user = event.getInteraction().getUser().getId();
          final Snowflake channelId = event.getInteraction().getChannelId();
          return event.deferEdit()
            .then(this.appeals.findByAppealDiscussionThreadAndResultIsNull(channelId))
            .flatMap(model -> {
              final Update updates = new Update();
              for (final AppealModel.Vote value : VOTES) {
                if (value != vote) {
                  updates.pull(AppealModel.Fields.votes(value), user);
                }
              }
              updates.push(AppealModel.Fields.votes(vote), user);
              return this.appeals.update(model._id(), updates);
            })
            .flatMap(model -> event.editReply().withComponents(createVoteButtons(model.votes())));
        }
        return Mono.empty();
      })
    );
  }

  private static String createChannelName(final Member member) {
    return "a-" + member.getId().asLong();
  }

  // TODO: does this need to be sanitized
  private static String createThreadName(final Member member) {
    return member.getUsername() + "#" + member.getDiscriminator();
  }

  Mono<AppealModel.Complete> findByAppealThread(final Snowflake channel) {
    return this.appeals.findByAppealThread(channel);
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private EmbedData createMessage(final MessageData message, final Optional<User> author, final Tuple2<Guild, GuildModel.Complete> guild) {
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

  private static ActionRow createVoteButtons(final Map<String, List<Snowflake>> votes) {
    return ActionRow.of(
      Arrays.stream(VOTES)
        .map(vote -> VOTE_BUTTON_FACTORY.get(vote).apply(
          VOTE_BUTTON_PREFIX + vote.words().button(),
          vote.emoji(),
          String.format("%s (%d)", vote.words().name(), votes.getOrDefault(vote.name(), List.of()).size())
        ))
        .toList()
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
              Emoji.toString(Emoji.CLOCK1),
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

    private static Instant resolveNextAttemptMayBeMadeAt(final Instant instant, final AppealModel.Result result) {
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
        Appeals.this.messageLinks.deleteAllByTargetChannelId(this.model.appealChannel().asLong()).onErrorResume(t -> Mono.empty()),
        Appeals.this.messageLinks.deleteAllByTargetChannelId(this.model.appealThread().asLong()).onErrorResume(t -> Mono.empty())
      );
    }

    private Mono<Void> unenforce() {
      if (this.result == AppealModel.Result.ACCEPTED) {
        final Mono<PunishmentModel.Complete> updatedPunishment = Appeals.this.punishments.update(this.model.punishment(), new PunishmentModel.Partial.Stale() {
          @Override
          public @NotNull Boolean stale() {
            return true;
          }

          @Override
          public Instant staleAt() {
            return Instant.now();
          }

          @Override
          public Snowflake staleById() {
            return AppealFinisher.this.user.getId();
          }

          @Override
          public String staleByUsername() {
            return AppealFinisher.this.user.getUsername();
          }

          @Override
          public String staleByDiscriminator() {
            return AppealFinisher.this.user.getDiscriminator();
          }

          @Override
          public String staleReason() {
            return AppealFinisher.this.reason;
          }

          @Override
          public Boolean staleAutomatic() {
            return AppealFinisher.this.automatic;
          }

          @Override
          public @Nullable ObjectId staleAppeal() {
            return AppealFinisher.this.model._id();
          }
        });
        return updatedPunishment
          .flatMap(punishment -> switch (punishment.type()) {
            case BAN -> this.client.getGuildById(punishment.guild()).flatMap(guild -> guild.unban(punishment.punishedId(), String.format(
              "Punishment (%s) has been appealed (%s).",
              punishment._id(),
              this.model._id()
            ))).onErrorResume(ClientException.class, t -> Mono.empty()); // avoid possible 10026
            default -> Mono.empty();
          });
      }
      return Mono.empty();
    }

    private Mono<Void> sendMessagesToChannelsAndThreadsAndThenArchiveAndClose() {
      final UnaryOperator<EmbedCreateSpec.Builder> embedForBoth = embed -> {
        return embed
          .color(Color.of(this.result.color()))
          .title("Appeal " + this.result.words().name());
      };
      final EmbedData embedForPunished = this.createEmbedForPunished(embedForBoth.apply(EmbedCreateSpec.builder())).asRequest();
      final EmbedData embedForStaff = this.createEmbedForStaff(embedForBoth.apply(EmbedCreateSpec.builder())).asRequest();
      final String reasonForActionLog = "Appeal has been " + this.result.words().name();
      return Appeals.this.guilds.findByGuild(this.model.guild())
        .flatMap(guildModel -> Mono.when(
          Mono.when(
            Mono.just(this.result)
              .filter(result -> result != AppealModel.Result.CANCELLED)
              .flatMap(result -> this.client.rest().getChannelById(this.model.appealChannel()).createMessage(embedForPunished)),
            Mono.just(this.result)
              .filter(result -> result == AppealModel.Result.ACCEPTED)
              .flatMap(accepted -> this.client.rest().getChannelById(this.model.appealChannel()).createMessage(guildModel.invite())),
            Mono.justOrEmpty(this.model.voteMessage()).flatMap(voteMessage -> this.client.rest().getMessageById(this.model.appealDiscussionThread(), voteMessage).edit(
              MessageEditRequest.builder()
                .components(Possible.of(Optional.empty()))
                .build()
            ))
          ).then(
            this.client.rest().getChannelById(this.model.appealChannel()).editChannelPermissions(
              this.model.user(),
              PermissionsEditRequest.builder()
                .type(PermissionOverwrite.Type.MEMBER.getValue())
                .allow(PermissionSet.of(Permission.VIEW_CHANNEL).getRawValue())
                .deny(PermissionSet.of(Permission.SEND_MESSAGES).getRawValue())
                .build(),
              reasonForActionLog
            )
          ),
          this.client.rest().getChannelById(guildModel.features().punishments().appeals().appealThreadsChannel()).createMessage(embedForStaff),
          this.client.rest().getChannelById(this.model.appealThread()).createMessage(embedForStaff).then(
            this.client.rest().getChannelService().modifyThread(
              this.model.appealThread().asLong(),
              ThreadModifyRequest.builder()
                .archived(true)
                .locked(true)
                .build(),
              reasonForActionLog
            )
          ),
          this.client.rest().getChannelById(this.model.appealDiscussionThread()).createMessage(embedForStaff).then(
            this.client.rest().getChannelService().modifyThread(
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
        case ACCEPTED -> "Your appeal has been approved. Please be sure you are up to date with the rules in our community prior to re-joining.";
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
        case ACCEPTED -> "The appeal was approved, and the user has been provided with information about re-joining the community.";
        case DENIED -> "The appeal was denied." + this.nextAttemptMayBeMadeAt("The");
        case CANCELLED -> "The appeal was cancelled.";
      });
      embed.addField(this.result.words().nameForStartOfSentence() + " by", Mention.userWithId(this.user.getId(), this.user.getUsername(), this.user.getDiscriminator()), false);
      if (this.reason != null) {
        embed.addField("Reason", this.reason, false);
      }
      if (this.automatic) {
        embed.addField("Automatic Decision", Emoji.toString(Emoji.YES), false);
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
