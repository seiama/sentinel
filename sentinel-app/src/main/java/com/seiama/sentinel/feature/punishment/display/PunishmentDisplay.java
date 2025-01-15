package com.seiama.sentinel.feature.punishment.display;

import com.seiama.sentinel.common.Thyme;
import com.seiama.sentinel.common.discord.Emoji;
import com.seiama.sentinel.common.discord.UserDisplay;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.common.model.UserIdentity;
import discord4j.common.util.TimestampFormat;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

public final class PunishmentDisplay {
  private static final String SYMBOL_AUTOMATIC = "(ᴀ)";
  private static final String SYMBOL_NOTIFIED = "(ɴ)";

  private PunishmentDisplay() {
  }

  public static EmbedCreateSpec punishment(final PunishmentModel.Complete punishment, final PunishmentDisplayStyle display) {
    final EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
    embed.color(Color.of(punishment.type().color()));
    if (display.type) embed.addField("Type", String.format("%s %s%s", punishment.type().emoji().asFormat(), punishment.type().strings().name(), automaticSuffix(punishment.automatic())), true);
    if (display.stale) embed.addField("Stale", Emoji.emoji(punishment.stale()).asFormat() + automaticSuffix(punishment.staleAutomatic()), true);
    if (display.expunged) embed.addField("Expunged", Emoji.emoji(punishment.expunged()).asFormat(), true);
    if (display.time) embed.addField("Time", TimestampFormat.LONG_DATE_TIME.format(punishment.date()), false);
    if (display.issuedBy) embed.addField("Issued by", UserDisplay.render(UserDisplay.Renderer.MENTION_WITH_TRAILING_BACKTICK_WRAPPED_USERNAME_AND_ID, punishment.punisher()), false);
    if (display.issuedTo) {
      final StringBuilder sb = new StringBuilder();
      sb.append(UserDisplay.render(UserDisplay.Renderer.MENTION_WITH_TRAILING_BACKTICK_WRAPPED_USERNAME_AND_ID, punishment.punished()));
      if (display.notified) {
        sb.append(
          punishment.dmNotificationMessageId() != null
            ? " " + SYMBOL_NOTIFIED
            : ""
        );
      }
      embed.addField("Issued to", sb.toString(), false);
    }
    if (display.reason) ifPresent(punishment.reason(), reason -> embed.addField("Reason", reason, false));
    if (display.duration) ifPresent(punishment.duration(), duration -> embed.addField("Duration", "~%s (expiry: %s)".formatted(
      Thyme.PRETTY_TIME.print(Thyme.ymwdhmsDuration(punishment.date(), punishment.date().plus(duration))),
      TimestampFormat.LONG_DATE_TIME.format(punishment.date().plus(duration))
    ), false));
    if (display.stale && Boolean.TRUE.equals(punishment.stale())) {
      ifPresent(punishment.staleAt(), at -> embed.addField("Stale time", TimestampFormat.LONG_DATE_TIME.format(at), false));
      ifPresent(punishment.staleById(), id -> embed.addField("Stale by", UserDisplay.render(UserDisplay.Renderer.MENTION_WITH_TRAILING_BACKTICK_WRAPPED_USERNAME_AND_ID, new UserIdentity(id, punishment.staleByUsername(), punishment.staleByDiscriminator())), false));
      ifPresent(punishment.staleReason(), reason -> embed.addField("Stale reason", reason, false));
    }
    embed.footer("Punishment: " + punishment._id(), null);
    return embed.build();
  }

  private static <T> void ifPresent(final @Nullable T value, final Consumer<T> consumer) {
    if (value != null) consumer.accept(value);
  }

  private static String automaticSuffix(final Boolean value) {
    if (value != null && value) {
      return " " + SYMBOL_AUTOMATIC;
    }
    return "";
  }
}
