package com.seiama.sentinel.feature.punishment;

import com.seiama.sentinel.common.CustomId;
import com.seiama.sentinel.common.Listener;
import com.seiama.sentinel.common.discord.Emojis;
import com.seiama.sentinel.common.discord.Links;
import com.seiama.sentinel.common.discord.UserDisplay;
import com.seiama.sentinel.common.model.Discriminator;
import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.common.model.ModMailModel;
import com.seiama.sentinel.common.model.ModMailRepository;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.object.command.Interaction;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.Button;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.discordjson.json.MessageEditRequest;
import discord4j.discordjson.json.StartThreadWithoutMessageRequest;
import java.time.Instant;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

@Component
@NullMarked
public class ModMail implements Listener {
  private static final int THREAD_AUTO_ARCHIVE_DURATION = 10080; // 7 days, in minutes
  private static final String BUTTON_CREATE_THREAD = "modmail/create-thread";
  private final GuildRepository guilds;
  private final ModMailRepository modmails;

  @Autowired
  public ModMail(final GuildRepository guilds, final ModMailRepository modmails) {
    this.guilds = guilds;
    this.modmails = modmails;
  }

  @Override
  public Mono<Void> listen(final GatewayDiscordClient client) {
    return client.on(ButtonInteractionEvent.class, event -> {
      final CustomId id = CustomId.parse(event.getCustomId());
      if (id != null && id.type().equals(BUTTON_CREATE_THREAD)) {
        final Interaction interaction = event.getInteraction();
        return event.edit()
          .withComponents(ActionRow.of(
            Button.primary(id.toString(), Emojis.PENCIL, "Create Thread").disabled(true)
          ))
          .then(
            this.modmails.findById(new ObjectId(id.value()))
              .zipWhen(model -> {
                return this.guilds.findByGuild(model.guild())
                  .flatMap(guildModel -> {
                    return client.rest().getChannelService().startThreadWithoutMessage(
                      guildModel.features().modmail().threadChannel().asLong(),
                      StartThreadWithoutMessageRequest.builder()
                        .type(Channel.Type.GUILD_PRIVATE_THREAD.getValue())
                        .name(UserDisplay.render(UserDisplay.Renderer.USERNAME, model.creator()))
                        .autoArchiveDuration(THREAD_AUTO_ARCHIVE_DURATION)
                        .build()
                    );
                  });
              })
              .flatMap(TupleUtils.function((model, thread) -> Mono.when(
                event.getMessage()
                  .map(Message::getData)
                  .filter(data -> !data.embeds().isEmpty())
                  .map(data -> client.rest().getChannelById(Snowflake.of(thread.id())).createMessage(data.embeds().get(0)))
                  .orElse(Mono.empty()),
                client.rest().getChannelService().addThreadMember(thread.id().asLong(), model.creatorId().asLong()),
                client.rest().getChannelService().addThreadMember(thread.id().asLong(), interaction.getUser().getId().asLong()),
                client.rest().getMessageById(interaction.getChannelId(), event.getMessageId()).edit(
                  MessageEditRequest.builder()
                    .componentsOrNull(
                      ActionRow.of(
                        Button.link(Links.channel(model.guild(), Snowflake.of(thread.id())), Emojis.PENCIL, "Go To Thread")
                      ).getData()
                    )
                    .build()
                ),
                this.modmails.update(model, new ModMailModel.Partial.ThreadCreated() {
                  @Override
                  public Instant threadCreatedAt() {
                    return Instant.now();
                  }

                  @Override
                  public Snowflake threadCreatedBy() {
                    return interaction.getUser().getId();
                  }
                })
              )))
          );
      }
      return Mono.empty();
    }).then();
  }

  public Mono<Void> create(final GatewayDiscordClient client, final Guild guild, final User user, final ModMailModel.Type type, final Message message) {
    return this.create(
      client,
      guild,
      user,
      type,
      message,
      message.getContent()
    );
  }

  public Mono<Void> create(final GatewayDiscordClient client, final Guild guild, final User user, final ModMailModel.Type type, final @Nullable Message message, final String content) {
    return this.guilds.findByGuild(guild.getId())
      .zipWhen(guildModel -> this.modmails.insert(new ModMailModel.Complete(
        new ObjectId(),
        guild.getId(),
        Instant.now(),
        type,
        user.getId(),
        user.getUsername(),
        new Discriminator(user),
        message != null ? message.getId() : null,
        content,
        null,
        null
      )))
      .flatMap(TupleUtils.function((guildModel, model) -> {
        final CustomId createThreadButton = new CustomId(BUTTON_CREATE_THREAD, model._id().toString());
        final EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder()
          .footer(String.format("%s (%s)", user.getTag(), user.getId().asString()), user.getAvatarUrl())
          .title(type.strings().submitted())
          .timestamp(Instant.now());
        final StringBuilder description = new StringBuilder();
        if (type == ModMailModel.Type.REPORT) {
          description.append("**Reported Content**:\n\n");
        }
        description.append(content);
        embed.description(description.toString());
        if (message != null) {
          message.getAuthor()
            .ifPresent(author -> embed.author(String.format("%s (%s)", author.getTag(), author.getId().asString()), null, author.getAvatarUrl()));
          embed.addField("Message", Links.message(guild, message), false);
        }
        return client.rest().getChannelById(guildModel.features().modmail().notificationChannel())
          .createMessage(
            MessageCreateSpec.builder()
              .embeds(embed.build())
              .components(ActionRow.of(
                Button.primary(createThreadButton.toString(), Emojis.PENCIL, "Create Thread")
              ))
              .build()
              .asRequest()
          );
      }))
      .then();
  }
}
