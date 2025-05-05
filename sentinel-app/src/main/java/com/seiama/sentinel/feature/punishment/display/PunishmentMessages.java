package com.seiama.sentinel.feature.punishment.display;

import com.seiama.sentinel.common.discord.Discord;
import com.seiama.sentinel.common.discord.Emojis;
import com.seiama.sentinel.common.model.PunishmentModel;
import discord4j.core.object.entity.Guild;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.MessageCreateSpec;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class PunishmentMessages {
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
          .color(punishment.type().color())
          .author(Discord.author(guild).orElse(null))
          .title("You've been " + punishment.type().strings().actioned() + ".")
          .addField("Reason", Objects.requireNonNullElse(punishment.reason(), REASON_NOT_SPECIFIED), false)
          .footer("Punishment: " + punishment._id(), null)
          .build()
      )
      .build();
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
}
