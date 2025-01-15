package com.seiama.sentinel.feature.punishment.display;

import com.seiama.sentinel.common.discord.Emoji;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.util.Mention;
import discord4j.common.util.TimestampFormat;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

public final class PunishmentDisplay {
  private static final String SYMBOL_AUTOMATIC = "(ᴀ)";

  private PunishmentDisplay() {
  }

  public static EmbedCreateSpec punishment(final PunishmentModel.Complete punishment, final PunishmentDisplayStyle display) {
    final EmbedCreateSpec.Builder embed = EmbedCreateSpec.builder();
    embed.color(Color.of(punishment.type().color()));
    if (display.type) embed.addField("Type", String.format("%s %s%s", Emoji.toString(punishment.type().emoji()), punishment.type().words().name(), automaticSuffix(punishment.automatic())), true);
    if (display.stale) embed.addField("Stale", Emoji.toString(Emoji.emoji(punishment.stale())) + automaticSuffix(punishment.staleAutomatic()), true);
    if (display.expunged) embed.addField("Expunged", Emoji.toString(Emoji.emoji(punishment.expunged())), true);
    if (display.time) embed.addField("Time", TimestampFormat.LONG_DATE_TIME.format(punishment.date()), false);
    if (display.issuedBy) embed.addField("Issued by", Mention.userWithId(punishment.punisherId(), punishment.punisherUsername(), punishment.punisherDiscriminator()), false);
    if (display.issuedTo) embed.addField("Issued to", Mention.userWithId(punishment.punishedId(), punishment.punisherUsername(), punishment.punishedDiscriminator()), false);
    if (display.reason) ifPresent(punishment.reason(), reason -> embed.addField("Reason", reason, false));
    if (display.stale && Boolean.TRUE.equals(punishment.stale())) {
      ifPresent(punishment.staleAt(), at -> embed.addField("Stale time", TimestampFormat.LONG_DATE_TIME.format(at), false));
      ifPresent(punishment.staleById(), id -> embed.addField("Stale by", Mention.userWithId(id, punishment.staleByUsername(), punishment.staleByDiscriminator()), false));
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
