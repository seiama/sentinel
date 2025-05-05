package com.seiama.sentinel.feature.punishment.appeal;

import com.seiama.sentinel.common.SharedConstants;
import com.seiama.sentinel.common.discord.Emojis;
import com.seiama.sentinel.common.discord.UserDisplay;
import com.seiama.sentinel.common.model.AppealModel;
import com.seiama.sentinel.common.model.AppealRepository;
import com.seiama.sentinel.common.model.Feature;
import com.seiama.sentinel.common.model.GuildModel;
import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.common.model.UserIdentity;
import com.seiama.sentinel.feature.punishment.Punishments;
import com.seiama.sentinel.feature.punishment.display.PunishmentDisplay;
import com.seiama.sentinel.feature.punishment.display.PunishmentDisplayStyle;
import com.seiama.sentinel.reactive.Reactive;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.channel.ThreadChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.util.MentionUtil;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.StartThreadWithoutMessageRequest;
import discord4j.discordjson.json.UserData;
import discord4j.rest.RestClient;
import discord4j.rest.http.client.ClientException;
import discord4j.rest.util.AllowedMentions;
import discord4j.rest.util.Color;
import java.time.Instant;
import java.util.Map;
import java.util.function.Function;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

@NullMarked
class StartHandler implements Function<MemberJoinEvent, Publisher<Void>> {
  private final GatewayDiscordClient client;

  private final GuildRepository guilds;
  private final Punishments punishmentOps;
  private final AppealRepository appeals;
  private final RestClient relayRest;

  StartHandler(
    final GatewayDiscordClient client,
    final GuildRepository guilds,
    final Punishments punishmentOps,
    final AppealRepository appeals,
    final RestClient relayRest
  ) {
    this.client = client;
    this.guilds = guilds;
    this.punishmentOps = punishmentOps;
    this.appeals = appeals;
    this.relayRest = relayRest;
  }

  @Override
  public Publisher<Void> apply(final MemberJoinEvent event) {
    return this.guilds.findByFeaturesPunishmentsAppealsGuild(event.getGuildId())
      .filter(Feature.APPEALS.enabledForGuild())
      .flatMap(guildModel -> {
        final ObjectId appealId = new ObjectId();
        final GuildModel.Complete.Features.Punishments.Appeals config = guildModel.features().punishments().appeals();
        final Member member = event.getMember();
        final Flux<PunishmentModel.Complete> getActiveBans = this.punishmentOps.findActive(guildModel.guild(), member.getId(), PunishmentModel.Type.BAN);

        final Mono<PunishmentModel.Complete> kickUserForNoActivePunishment = event.getGuild()
          .flatMap(guild -> guild.kick(member.getId(), "Could not find an active ban."))
          .then(Mono.empty());

        return getActiveBans.next()
          .switchIfEmpty(kickUserForNoActivePunishment)
          .flatMap(punishment -> {
            final DiscordClient rest = this.client.rest();

            final Mono<TextChannel> createAppealChannel = event.getGuild()
              .flatMap(guild -> guild.createTextChannel(Appeals.createChannelName(member, appealId))
                .withParentId(config.appealChannelsCategory())
                .withPermissionOverwrites(Appeals.createPermissionOverwrites(config.everyoneRole(), member.getId(), false))
                .withRateLimitPerUser(Appeals.CHANNEL_RATE_LIMIT)
                .withTopic("Appeal channel for %s".formatted(member.getMention()))
              );

            if (Boolean.TRUE.equals(punishment.permanent())) {
              return Mono.when(
                createAppealChannel
                  .flatMap(channel -> Mono.when(
                    channel.createMessage("Your active ban is currently not eligible for appeal."),
                    channel.edit()
                      .withPermissionOverwrites(Appeals.createPermissionOverwrites(config.everyoneRole(), member.getId(), true))
                      .withReason(String.format("Punishment %s is not eligible for appeal.", punishment._id()))
                      .onErrorResume(ClientException.class, Reactive.<TextChannel>ignoringException()), // avoid possible 10009 if the user has left the guild
                    rest.getChannelById(config.appealThreadsChannel()).createMessage(String.format(
                      "%s attempted to appeal punishment `%s`, but the appeal was automatically rejected because this type of punishment is not eligible for appeal.",
                      UserDisplay.render(UserDisplay.Renderer.MENTION_WITH_TRAILING_BACKTICK_WRAPPED_USERNAME_AND_ID, new UserIdentity(member)),
                      punishment._id()
                    ))
                  ))
              );
            }

            final Mono<ChannelData> createAppealThread = this.client.rest().getChannelService().startThreadWithoutMessage(
              config.appealThreadsChannel().asLong(),
              StartThreadWithoutMessageRequest.builder()
                .type(Channel.Type.GUILD_PRIVATE_THREAD.getValue())
                .name(Appeals.createThreadName(member))
                .autoArchiveDuration(Appeals.THREAD_AUTO_ARCHIVE_DURATION)
                .build()
            );
            final Mono<ChannelData> createAppealDiscussionThread = this.client.rest().getChannelService().startThreadWithoutMessage(
              config.appealDiscussionThreadsChannel().asLong(),
              StartThreadWithoutMessageRequest.builder()
                .type(Channel.Type.GUILD_PRIVATE_THREAD.getValue())
                .name(Appeals.createThreadName(member))
                .autoArchiveDuration(Appeals.THREAD_AUTO_ARCHIVE_DURATION)
                .build()
            );
            return Reactive.zipSequence(
                createAppealChannel,
                createAppealThread,
                createAppealDiscussionThread,
                this.relayRest.getSelf()
              )
              .zipWhen(TupleUtils.function((channel, appealThread, appealDiscussionThread, relayUser) -> this.appeals.insert(new AppealModel.Complete(
                appealId,
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
                final Tuple4<TextChannel, ChannelData, ChannelData, UserData> t1 = tuple.getT1();
                return Tuples.of(t1.getT1(), t1.getT2(), t1.getT3(), t1.getT4(), tuple.getT2());
              })
              .flatMap(TupleUtils.function((channel, appealThread, appealDiscussionThread, relayUser, model) -> {
                final Snowflake appealThreadId = Snowflake.of(appealThread.id());
                final Snowflake appealDiscussionThreadId = Snowflake.of(appealDiscussionThread.id());

                return Mono.when(
                  rest.getChannelService().addThreadMember(appealThread.id().asLong(), relayUser.id().asLong()),
                  channel.createMessage().withFlags(Message.Flag.IS_COMPONENTS_V2).withAllowedMentions(AllowedMentions.suppressAll()).withComponents(PunishmentDisplay.punishment(punishment, PunishmentDisplayStyle.APPEAL)),
                  channel.createMessage(String.format("Hey, %s! This appeal is now active. Please explain why you think this punishment should be appealed.", member.getMention())),
                  this.client.getChannelById(appealThreadId)
                    .ofType(ThreadChannel.class)
                    .flatMapMany(appealThreadChannel -> Flux.just(
                      appealThreadChannel.createMessage()
                        .withAllowedMentions(AllowedMentions.suppressAll())
                        .withFlags(Message.Flag.IS_COMPONENTS_V2)
                        .withComponents(PunishmentDisplay.punishment(punishment, PunishmentDisplayStyle.FULL)),
                      appealThreadChannel.createMessage()
                        .withEmbeds(
                          EmbedCreateSpec.builder()
                            .color(SharedConstants.COLOR_YELLOW)
                            .title("%1$s WARNING %1$s".formatted(Emojis.WARNING.asFormat()))
                            .description("Please note that any messages sent in this channel will be shared with the user who is appealing. For staff discussion, please use %s.".formatted(
                              MentionUtil.forChannel(Snowflake.of(appealDiscussionThread.id()))
                            ))
                            .build()
                        )
                    )),
                  this.client.getChannelById(appealDiscussionThreadId)
                    .ofType(ThreadChannel.class)
                    .flatMapMany(appealDiscussionThreadChannel -> Flux.just(
                      appealDiscussionThreadChannel.createMessage()
                        .withEmbeds(
                          EmbedCreateSpec.builder()
                            .color(SharedConstants.COLOR_BLUE)
                            .title("Appeal channel")
                            .description(MentionUtil.forChannel(appealThreadId))
                            .build()
                        )
                        .flatMap(Message::pin),
                      this.punishmentOps.repository().findAllByGuildAndPunishedIdOrderByDateDesc(punishment.guild(), member.getId())
                        .filter(item -> item.type() != PunishmentModel.Type.NOTE)
                        .collectList()
                        .flatMap(punishments -> {
                          return appealDiscussionThreadChannel.createMessage()
                            .withFlags(Message.Flag.IS_COMPONENTS_V2)
                            .withComponents(PunishmentDisplay.history(member, punishments))
                            .flatMap(Message::pin);
                        }),
                      appealDiscussionThreadChannel.createMessage()
                        .withComponents(Appeals.createVoteButtons(Map.of()))
                        .withEmbeds(Appeals.createVoteSummary(Map.of()))
                        .flatMap(voteMessage -> Mono.when(
                          voteMessage.pin(),
                          this.appeals.update(model._id(), (AppealModel.Partial.VoteMessage) () -> voteMessage.getId())
                        ))
                    )),
                  rest.getChannelById(config.appealThreadsChannel()).createMessage(
                    EmbedCreateSpec.builder()
                      .color(Color.of(Appeals.NEW_APPEAL_NOTIFICATION_COLOR))
                      .title("A new appeal has been created")
                      .description(String.format("A new appeal has been created by %s.", UserDisplay.render(UserDisplay.Renderer.MENTION_WITH_TRAILING_BACKTICK_WRAPPED_USERNAME_AND_ID, new UserIdentity(member))))
                      .addField("Appeal channel", MentionUtil.forChannel(appealThreadId), false)
                      .addField("Discussion channel", MentionUtil.forChannel(Snowflake.of(appealDiscussionThread.id())), false)
                      .build()
                      .asRequest()
                  )
                );
              }));
          });
      });
  }
}
