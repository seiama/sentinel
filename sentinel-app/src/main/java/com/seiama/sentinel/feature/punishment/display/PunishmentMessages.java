package com.seiama.sentinel.feature.punishment.display;

import com.seiama.sentinel.common.discord.Discord;
import com.seiama.sentinel.common.discord.Emojis;
import com.seiama.sentinel.common.discord.UserDisplay;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.feature.punishment.search.PunishmentSearchResult;
import discord4j.common.util.TimestampFormat;
import discord4j.core.object.entity.Guild;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.rest.util.Color;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class PunishmentMessages {
  private static final int MAX_REASON_LENGTH_IN_PUNISHMENT_SEARCH = 45;

  private static final String REASON_NOT_SPECIFIED = "(not specified)";
  public static final String PUNISHMENT_NOT_FOUND = "Could not find a punishment with the specified id.";

  private PunishmentMessages() {
  }

  public static String enforcingExisting(final PunishmentModel.Complete punishment) {
    return "Enforcing punishment %s".formatted(punishment._id());
  }

  public static MessageCreateSpec punishmentPunishedDirectMessageEmbed(final PunishmentModel.Complete punishment, final @Nullable Guild guild) {
    return MessageCreateSpec.builder()
      .addEmbed(
        EmbedCreateSpec.builder()
          .color(Color.of(punishment.type().color()))
          .author(Discord.author(guild).orElse(null))
          .title("You've been " + punishment.type().strings().actioned() + ".")
          .addField("Reason", Objects.requireNonNullElse(punishment.reason(), REASON_NOT_SPECIFIED), false)
          .footer("Punishment: " + punishment._id(), null)
          .build()
      )
      .build();
  }

  public static String punishmentPunisherResponse(final PunishmentModel.Complete punishment) {
    final @Nullable String reason = punishment.reason();
    return String.format(
      "(`%s`) // %s has been %s by %s (`%d`)%s",
      punishment._id(),
      UserDisplay.render(UserDisplay.Renderer.MENTION_WITH_TRAILING_BACKTICK_WRAPPED_USERNAME_AND_ID, punishment.punished()),
      punishment.type().strings().actioned(),
      UserDisplay.render(UserDisplay.Renderer.MENTION, punishment.punisher()),
      punishment.punisherId().asLong(),
      reason != null
        ? "\n**Reason**: " + reason
        : ""
    );
  }

  public static String punishmentUpdated(final PunishmentModel.Complete punishment) {
    return String.format("Punishment `%s` has been updated.", punishment._id());
  }

  public static String punishmentMarkedStale(final PunishmentModel.Complete punishment) {
    return String.format("Punishment `%s` has been marked stale.", punishment._id());
  }

  public static String punishmentPunishedReason(final PunishmentModel.Complete punishment) {
    return "(%s) %s".formatted(
      punishment._id(),
      Objects.requireNonNullElse(punishment.reason(), REASON_NOT_SPECIFIED)
        .replace("\r\n", " ")
        .replace("\n", " ")
    );
  }

  public static String mayNotPunish() {
    return "%s That user may not be punished.".formatted(Emojis.NO.asFormat());
  }

  public static EmbedCreateSpec punishmentSearchEmbed(final PunishmentSearchResult result, final String title) {
    final EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
    embed.author(Discord.author(result.user()).orElse(null));
    embed.title(title);
    final String description = result.punishments().stream()
      .map(punishment -> {
        final StringBuilder sb = new StringBuilder();
        final String reason = punishment.reason();
        if (reason != null) {
          sb.append("\n→ `");
          sb.append(reason, 0, Math.min(MAX_REASON_LENGTH_IN_PUNISHMENT_SEARCH, reason.length()));
          sb.append("`");
          if (reason.length() > MAX_REASON_LENGTH_IN_PUNISHMENT_SEARCH) {
            sb.append("…");
          }
        }
        return String.format(
          "%s %s (`%s`) %s%s",
          punishment.type().emoji().asFormat(),
          punishment.type().strings().actioned(),
          punishment._id(),
          TimestampFormat.LONG_DATE_TIME.format(punishment.date()),
          sb
        );
      })
      .collect(Collectors.joining("\n"));

    if (!description.isEmpty()) {
      embed.description(description);
    } else {
      embed.description("Squeaky clean record.  " + Emojis.TADA.asFormat());
    }
    return embed.build();
  }
}
