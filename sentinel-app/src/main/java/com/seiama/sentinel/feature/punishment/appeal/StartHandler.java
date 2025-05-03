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
import com.seiama.sentinel.feature.punishment.display.PunishmentMessages;
import com.seiama.sentinel.feature.punishment.search.PunishmentSearchResult;
import com.seiama.sentinel.reactive.Reactive;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.util.MentionUtil;
import discord4j.discordjson.json.ChannelData;
import discord4j.discordjson.json.StartThreadWithoutMessageRequest;
import discord4j.discordjson.json.UserData;
import discord4j.rest.RestClient;
import discord4j.rest.entity.RestChannel;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import java.time.Instant;
import java.util.Map;
import java.util.function.Function;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuple5;
import reactor.util.function.Tuples;

@NullMarked
class StartHandler implements Function<MemberJoinEvent, Publisher<Void>> {
  private final GatewayDiscordClient client;

  private final GuildRepository guilds;
  private final Punishments punishmentOps;
  private final AppealRepository appeals;
  private final RestClient relayRest;

  StartHandler(final GatewayDiscordClient client, final GuildRepository guilds, final Punishments punishmentOps, final AppealRepository appeals, final RestClient relayRest) {
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
        final Mono<TextChannel> createAppealChannel = event.getGuild()
          .flatMap(guild -> guild.createTextChannel(Appeals.createChannelName(member, appealId))
            .withParentId(config.appealChannelsCategory())
            .withPermissionOverwrites(
              PermissionOverwrite.forMember(member.getId(), PermissionSet.of(Permission.VIEW_CHANNEL), PermissionSet.none()),
              PermissionOverwrite.forRole(config.everyoneRole(), PermissionSet.none(), PermissionSet.of(Permission.VIEW_CHANNEL, Permission.ATTACH_FILES))
            )
            .withRateLimitPerUser(Appeals.CHANNEL_RATE_LIMIT)
            .withTopic("Appeal channel for %s".formatted(member.getMention()))
          );
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
            getActiveBans
              .next()
              .switchIfEmpty(kickUserForNoActivePunishment),
            createAppealChannel,
            createAppealThread,
            createAppealDiscussionThread,
            this.relayRest.getSelf()
          )
          .zipWhen(TupleUtils.function((punishment, channel, appealThread, appealDiscussionThread, relayUser) -> this.appeals.insert(new AppealModel.Complete(
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
            final Tuple5<PunishmentModel.Complete, TextChannel, ChannelData, ChannelData, UserData> t1 = tuple.getT1();
            return Tuples.of(t1.getT1(), t1.getT2(), t1.getT3(), t1.getT4(), t1.getT5(), tuple.getT2());
          })
          .flatMap(TupleUtils.function((punishment, channel, appealThread, appealDiscussionThread, relayUser, model) -> {
            final DiscordClient rest = this.client.rest();
            final RestChannel appealThreadChannel = rest.getChannelById(Snowflake.of(appealThread.id()));
            final RestChannel appealDiscussionThreadChannel = rest.getChannelById(Snowflake.of(appealDiscussionThread.id()));
            return Mono.when(
              rest.getChannelService().addThreadMember(appealThread.id().asLong(), relayUser.id().asLong()),
              channel.createMessage(PunishmentDisplay.punishment(punishment, PunishmentDisplayStyle.APPEAL)),
              channel.createMessage(String.format("Hey, %s! This appeal is now active. Please explain why you think this punishment should be appealed.", member.getMention())),
              appealThreadChannel.createMessage(PunishmentDisplay.punishment(punishment, PunishmentDisplayStyle.FULL).asRequest()),
              appealThreadChannel.createMessage(
                EmbedCreateSpec.builder()
                  .color(Color.of(SharedConstants.COLOR_YELLOW))
                  .title("%1$s WARNING %1$s".formatted(Emojis.WARNING.asFormat()))
                  .description("Please note that any messages sent in this channel will be shared with the user who is appealing. For staff discussion, please use %s.".formatted(
                    MentionUtil.forChannel(Snowflake.of(appealDiscussionThread.id()))
                  ))
                  .build()
                  .asRequest()
              ),
              appealDiscussionThreadChannel.createMessage(
                EmbedCreateSpec.builder()
                  .color(Color.of(SharedConstants.COLOR_BLUE))
                  .title("Appeal channel")
                  .description(MentionUtil.forChannel(Snowflake.of(appealThread.id())))
                  .build()
                  .asRequest()
              ).flatMap(voteMessage -> rest.getChannelService().addPinnedMessage(appealDiscussionThread.id().asLong(), voteMessage.id().asLong())),
              this.punishmentOps.repository().findAllByGuildAndPunishedIdOrderByDateDesc(punishment.guild(), member.getId())
                .filter(item -> item.type() != PunishmentModel.Type.NOTE)
                .collectList()
                .map(punishments -> new PunishmentSearchResult(member, punishments))
                .flatMap(psr -> {
                  return appealDiscussionThreadChannel.createMessage(
                    MessageCreateSpec.builder()
                      .addEmbed(
                        PunishmentMessages.punishmentSearchEmbed(psr, "Punishment history")
                      )
                      .build()
                      .asRequest()
                  ).flatMap(voteMessage -> Mono.when(
                    rest.getChannelService().addPinnedMessage(appealDiscussionThread.id().asLong(), voteMessage.id().asLong()),
                    this.appeals.update(model._id(), (AppealModel.Partial.VoteMessage) () -> Snowflake.of(voteMessage.id()))
                  ));
                }),
              appealDiscussionThreadChannel.createMessage(
                MessageCreateSpec.builder()
                  .addAllEmbeds(Appeals.createVoteSummary(Map.of()))
                  .addComponent(Appeals.createVoteButtons(Map.of()))
                  .build()
                  .asRequest()
              ).flatMap(voteMessage -> Mono.when(
                rest.getChannelService().addPinnedMessage(appealDiscussionThread.id().asLong(), voteMessage.id().asLong()),
                this.appeals.update(model._id(), (AppealModel.Partial.VoteMessage) () -> Snowflake.of(voteMessage.id()))
              )),
              rest.getChannelById(config.appealThreadsChannel()).createMessage(
                EmbedCreateSpec.builder()
                  .color(Color.of(Appeals.NEW_APPEAL_NOTIFICATION_COLOR))
                  .title("A new appeal has been created")
                  .description(String.format("A new appeal has been created by %s.", UserDisplay.render(UserDisplay.Renderer.MENTION_WITH_TRAILING_BACKTICK_WRAPPED_USERNAME_AND_ID, new UserIdentity(member))))
                  .addField("Appeal channel", MentionUtil.forChannel(Snowflake.of(appealThread.id())), false)
                  .addField("Discussion channel", MentionUtil.forChannel(Snowflake.of(appealDiscussionThread.id())), false)
                  .build()
                  .asRequest()
              )
            );
          }));
      });
  }
}
