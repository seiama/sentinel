package com.seiama.sentinel.feature.application;

import com.seiama.sentinel.common.Listener;
import com.seiama.sentinel.common.model.AppealModel;
import com.seiama.sentinel.common.model.ApplicationModel;
import com.seiama.sentinel.common.model.ApplicationRepository;
import com.seiama.sentinel.common.model.GuildRepository;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ButtonInteractionEvent;
import discord4j.core.event.domain.interaction.ModalSubmitInteractionEvent;
import discord4j.core.event.domain.poll.PollVoteAddEvent;
import discord4j.core.object.component.Label;
import discord4j.core.object.component.TextInput;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.core.object.entity.poll.PollAnswer;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionPresentModalSpec;
import discord4j.core.spec.PollCreateSpec;
import discord4j.core.util.MentionUtil;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

@Component
@NullMarked
public class ApplicationListener implements Listener {

  public static final String OPEN_MODAL_BUTTON_ID = "open_application_form_button";
  public static final String MODAL_ID = "application_form_modal";
  public static final String GITHUB_PROFILE_INPUT_ID = "github_profile_input";

  private static final Pattern GITHUB_PROFILE_PATTERN = Pattern.compile("https://github\\.com/[A-Za-z0-9_-]+/?");
  private static final Pattern PR_LINK_PATTERN = Pattern.compile("https://github\\.com/[A-Za-z0-9_-]+/[A-Za-z0-9_-]+/pull/\\d+/?");

  private final GuildRepository guilds;
  private final ApplicationRepository applications;

  @Autowired
  public ApplicationListener(GuildRepository guilds, ApplicationRepository applications) {
    this.guilds = guilds;
    this.applications = applications;
  }

  @Override
  public Mono<Void> listen(final GatewayDiscordClient client) {
    return Mono.when(
      client.on(ButtonInteractionEvent.class, event -> {
        if (!event.getCustomId().equals(OPEN_MODAL_BUTTON_ID)) {

          return Mono.empty();
        }
        var modal = InteractionPresentModalSpec.builder()
          .title("Apply for the contributor role")
          .customId(MODAL_ID)
          .addAllComponents(List.of(
            Label.of("Link to your GitHub Profile", TextInput.small(GITHUB_PROFILE_INPUT_ID).required().placeholder("https://github.com/username")),
            Label.of("Link to your last PR", "Note that we are mostly interested in regular contributors of non trivial things.", TextInput.small("last_pr_input").placeholder("https://github.com/PaperMC/Paper/pull/XXXX")
            )))
          .build();

        // cant defer modals :/
        return event.getInteraction().getGuild()
          .flatMap(guild -> this.applications.findByGuildAndUserOrderByDate(guild.getId(), event.getUser().getId()))
          .flatMap(application -> {
            if (application.nextAttemptMayBeMadeAt() != null) {
              if (application.nextAttemptMayBeMadeAt().isBefore(Instant.now())) {
                return event.presentModal(modal);
              } else {
                return event.reply("You have already submitted an application recently. Please wait before submitting another application.");
              }
            } else {
              return event.reply("You have already submitted an application. Please wait for it to be reviewed.");
            }
          })
          .switchIfEmpty(event.presentModal(modal))
          .then();
      }),

      client.on(ModalSubmitInteractionEvent.class, event -> {
        if (event.getCustomId().equals(MODAL_ID)) {
          String githubProfile = null;
          String lastPr = null;
          for (TextInput input : event.getComponents(TextInput.class)) {
            if (input.getCustomId().equals(GITHUB_PROFILE_INPUT_ID)) {
              githubProfile = input.getValue().orElse(null);
            } else if (input.getCustomId().equals("last_pr_input")) {
              lastPr = input.getValue().orElse(null);
            }
          }
          final String fGithubProfile = githubProfile;
          final String fLastPr = lastPr;

          if (fGithubProfile == null || fLastPr == null) {
            return event.reply("You must fill out all fields in the application form.").withEphemeral(true).then();
          }
          if (!GITHUB_PROFILE_PATTERN.matcher(fGithubProfile).matches()) {
            return event.reply("The GitHub profile link you provided is not valid. Please provide a valid link.").withEphemeral(true).then();
          }
          if (!PR_LINK_PATTERN.matcher(fLastPr).matches()) {
            return event.reply("The last PR link you provided is not valid. Please provide a valid link.").withEphemeral(true).then();
          }

          return event.deferReply().withEphemeral(true)
            .then(event.getInteraction().getGuild()
              .flatMap(guild -> this.guilds.findByGuild(guild.getId())
                .flatMap(guildConfig -> guild.getChannelById(guildConfig.features().application().applicationChannel())
                  .cast(TextChannel.class)
                  .flatMap(channel ->
                    channel.createMessage(EmbedCreateSpec.builder()
                        .title("New Contributor Role Application")
                        .addField("User: ", MentionUtil.forUser(event.getUser().getId()), false)
                        .addField("GitHub Profile: ", fGithubProfile, false)
                        .addField("Last PR: ", fLastPr, false)
                        .build())
                      .then(channel.createPoll(PollCreateSpec.builder()
                          .question("Grant contributor role to " + event.getUser().getUsername())
                          // todo fix emoji
                          .addAllAnswers(ApplicationModel.Vote.all().map(s -> PollAnswer.of(s.label() /*, s.emoji() */)).toList())
                          .duration(48)
                          .build())
                        .flatMap((poll) -> this.applications.insert(new ApplicationModel.Complete(
                          ObjectId.get(),
                          guild.getId(),
                          Instant.now(),
                          event.getUser().getId(),
                          poll.getId(),
                          fGithubProfile,
                          fLastPr,
                          new HashMap<>(),
                          null,
                          null
                        ))))
                      .flatMap(application -> event.editReply("Thank you for your application! We will review your application " + application.githubProfile() + " within the next few days."))
                  ))
              )
            ).then();
        }
        return Mono.empty();
      }),

      client.on(PollVoteAddEvent.class, event -> {
        // TODO check if somebody clicked insta approve and has permissions for that
        // TODO close poll, add role to user, persist in db
        return event.getAnswer().then();
      })

      // TODO if poll ended grant role
//      client.on(PollEnded?!)
    );
  }
}
