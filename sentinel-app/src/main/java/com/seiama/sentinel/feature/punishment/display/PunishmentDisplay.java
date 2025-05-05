package com.seiama.sentinel.feature.punishment.display;

import com.seiama.sentinel.common.Thyme;
import com.seiama.sentinel.common.discord.Components;
import com.seiama.sentinel.common.discord.Emojis;
import com.seiama.sentinel.common.discord.UserDisplay;
import com.seiama.sentinel.common.model.Discriminator;
import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.common.model.UserIdentity;
import discord4j.common.util.Snowflake;
import discord4j.common.util.TimestampFormat;
import discord4j.core.object.component.Container;
import discord4j.core.object.component.Separator;
import discord4j.core.object.component.TextDisplay;
import discord4j.core.object.component.Thumbnail;
import discord4j.core.object.component.UnfurledMediaItem;
import discord4j.core.object.entity.User;
import discord4j.core.util.MentionUtil;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.OptionalInt;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class PunishmentDisplay {
  private static final String SYMBOL_AUTOMATIC = "(Automatic)";
  private static final String SYMBOL_NOTIFIED = "(Notified)";

  public static final int HISTORY_PAGE_SIZE = 10;

  private PunishmentDisplay() {
  }

  public static Container punishment(final PunishmentModel.Complete punishment, final PunishmentDisplayStyle display) {
    return Components.container(OptionalInt.empty(), container -> {
      container.add(TextDisplay.of(String.format(
        "### Punishment %s",
        punishment._id()
      )));
      if (display.type) {
        container.add(TextDisplay.of(field(
          "Type",
          Stream.of(
            punishment.type().emoji().asFormat(),
            punishment.type().strings().name(),
            Boolean.TRUE.equals(punishment.automatic()) ? SYMBOL_AUTOMATIC : null
          )
        )));
      }
      if (display.stale) {
        container.add(TextDisplay.of(field(
          "Stale",
          Stream.of(
            Emojis.emoji(punishment.stale()).asFormat(),
            Boolean.TRUE.equals(punishment.staleAutomatic()) ? SYMBOL_AUTOMATIC : null
          )
        )));
      }
      if (display.expunged) {
        container.add(TextDisplay.of(field(
          "Expunged",
          Stream.of(
            Emojis.emoji(punishment.expunged()).asFormat()
          )
        )));
      }
      if (display.permanent) {
        container.add(TextDisplay.of(field(
          "Permanent",
          Stream.of(
            Emojis.emoji(punishment.permanent()).asFormat()
          )
        )));
      }
      if (display.time) {
        container.add(TextDisplay.of(field(
          "Time",
          Stream.of(
            TimestampFormat.LONG_DATE_TIME.format(punishment.date())
          )
        )));
      }
      if (display.issuedBy) {
        container.add(TextDisplay.of(field(
          "Issued by",
          Stream.of(
            UserDisplay.render(UserDisplay.Renderer.MENTION_WITH_TRAILING_BACKTICK_WRAPPED_USERNAME_AND_ID, punishment.punisher())
          )
        )));
      }
      if (display.issuedTo) {
        container.add(TextDisplay.of(field(
          "Issued to",
          Stream.of(
            UserDisplay.render(UserDisplay.Renderer.MENTION_WITH_TRAILING_BACKTICK_WRAPPED_USERNAME_AND_ID, punishment.punished()),
            display.notified && punishment.wasNotified() ? SYMBOL_NOTIFIED : null
          )
        )));
      }
      if (display.reason) {
        final String reason = punishment.reason();
        if (reason != null) {
          container.add(TextDisplay.of(field(
            "Reason",
            Stream.of(
              reason
            )
          )));
        }
      }
      if (display.duration) {
        final Duration duration = punishment.duration();
        if (duration != null) {
          container.add(TextDisplay.of(field(
            "Duration",
            Stream.of(
              "~%s (expiry: %s)".formatted(
                Thyme.PRETTY_TIME.print(Thyme.ymwdhmsDuration(punishment.date(), punishment.date().plus(duration))),
                TimestampFormat.LONG_DATE_TIME.format(punishment.date().plus(duration))
              )
            )
          )));
        }
      }
      if (display == PunishmentDisplayStyle.LOG) {
        final Snowflake privateNotificationThreadId = punishment.privateNotificationThreadId();
        if (privateNotificationThreadId != null) {
          container.add(TextDisplay.of(field(
            "Notification Thread",
            Stream.of(
              MentionUtil.forChannel(privateNotificationThreadId)
            )
          )));
        }
      }
      if (display.stale && punishment.stale()) {
        final Instant staleAt = punishment.staleAt();
        if (staleAt != null) {
          container.add(TextDisplay.of(field(
            "Stale time",
            Stream.of(
              TimestampFormat.LONG_DATE_TIME.format(staleAt)
            )
          )));
        }
        final Snowflake staleById = punishment.staleById();
        final String staleByUsername = punishment.staleByUsername();
        final Discriminator staleByDiscriminator = punishment.staleByDiscriminator();
        if (staleById != null && staleByUsername != null && staleByDiscriminator != null) {
          container.add(TextDisplay.of(field(
            "Stale by",
            Stream.of(
              UserDisplay.render(UserDisplay.Renderer.MENTION_WITH_TRAILING_BACKTICK_WRAPPED_USERNAME_AND_ID, new UserIdentity(staleById, staleByUsername, staleByDiscriminator))
            )
          )));
        }
        final String staleReason = punishment.staleReason();
        if (staleReason != null) {
          container.add(TextDisplay.of(field(
            "Stale reason",
            Stream.of(
              staleReason
            )
          )));
        }
      }
    }, punishment.type().color(), false);
  }

  public static Container history(final User user, final List<PunishmentModel.Complete> punishments) {
    return Components.container(OptionalInt.empty(), container -> {
      container.add(Components.section(OptionalInt.empty(), section -> {
        section.add(TextDisplay.of(String.format(
          "## Punishment history for %s",
          user.getTag()
        )));
      }, Thumbnail.of(UnfurledMediaItem.of(user.getAvatarUrl()))));
      container.add(Separator.of());
      if (!punishments.isEmpty()) {
        for (final PunishmentModel.Complete punishment : punishments) {
          container.add(TextDisplay.of(String.format(
            "%s %s (`%s`) %s",
            punishment.type().emoji().asFormat(),
            punishment.type().strings().actioned(),
            punishment._id(),
            TimestampFormat.LONG_DATE_TIME.format(punishment.date())
          )));
          final String reason = punishment.reason();
          if (reason != null) {
            container.add(TextDisplay.of(String.format(
              "⟶ `%s`",
              reason
            )));
          }
        }
      } else {
        container.add(TextDisplay.of("No results found."));
      }
    }, null, false);
  }

  public static TextDisplay updated(final PunishmentModel.Complete punishment) {
    return TextDisplay.of(String.format(
      "Punishment `%s` was successfully updated.",
      punishment._id()
    ));
  }

  private static String field(final String name, final Stream<@Nullable String> values) {
    return String.format(
      "**%s**: %s",
      name,
      values
        .filter(string -> string != null && !string.isBlank())
        .collect(Collectors.joining(" "))
    );
  }
}
