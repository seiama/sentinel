package com.seiama.sentinel.feature.punishment.display;

import com.seiama.sentinel.common.discord.Discord;
import com.seiama.sentinel.common.discord.Emoji;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.feature.punishment.search.PunishmentSearchResult;
import discord4j.common.util.TimestampFormat;
import discord4j.core.object.entity.Guild;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import discord4j.core.util.MentionUtil;
import discord4j.rest.util.Color;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

public final class PunishmentMessages {
  private static final int MAX_REASON_LENGTH_IN_PUNISHMENT_SEARCH = 45;

  private static final String REASON_NOT_SPECIFIED = "(not specified)";
  public static final String PUNISHMENT_NOT_FOUND = "Could not find a punishment with the specified id.";

  private PunishmentMessages() {
  }

  public static MessageCreateSpec punishmentPunishedDirectMessageEmbed(final PunishmentModel.Complete punishment, final @Nullable Guild guild) {
    return MessageCreateSpec.builder()
      .addEmbed(
        EmbedCreateSpec.builder()
          .color(Color.of(punishment.type().color()))
          .author(Discord.author(guild).orElse(null))
          .title("You've been " + punishment.type().words().actioned() + ".")
          .addField("Reason", Objects.requireNonNullElse(punishment.reason(), REASON_NOT_SPECIFIED), false)
          .footer("Punishment: " + punishment._id(), null)
          .build()
      )
      .build();
  }

  public static String punishmentPunisherResponse(final PunishmentModel.Complete punishment) {
    return String.format(
      "(`%s`) // %s (`%d`) has been %s by %s (`%d`)",
      punishment._id(),
      MentionUtil.forUser(punishment.punishedId()),
      punishment.punishedId().asLong(),
      punishment.type().words().actioned(),
      MentionUtil.forUser(punishment.punisherId()),
      punishment.punisherId().asLong()
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
    );
  }

  public static EmbedCreateSpec punishmentSearchEmbed(final PunishmentSearchResult result) {
    final EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
    embed.author(Discord.author(result.user()).orElse(null));
    embed.title("Punishment search results");
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
          punishment.type().words().actioned(),
          punishment._id(),
          TimestampFormat.LONG_DATE_TIME.format(punishment.date()),
          sb
        );
      })
      .collect(Collectors.joining("\n"));

    if (!description.isEmpty()) {
      embed.description(description);
    } else {
      embed.description("Squeaky clean record.  " + Emoji.TADA.asFormat());
    }
    return embed.build();
  }
}
