package com.seiama.sentinel.common.model;

import com.seiama.functional.function.exceptional.Consumer1E;
import com.seiama.functional.function.exceptional.RunnableE;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.UserData;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Deprecated
@NullMarked
public record Discriminator(
  @Nullable String value
) {
  // https://support-dev.discord.com/hc/en-us/articles/13667755828631#h_01GYA87X9QZ19H2X6PJ3M35ZDH
  @VisibleForTesting
  public static final String TEMPORARY_MIGRATION_MARKER = "0";

  public Discriminator(final UserData user) {
    this(user.discriminator());
  }

  public Discriminator(final User user) {
    this(user.getDiscriminator());
  }

  public boolean migrated() {
    return this.value == null || TEMPORARY_MIGRATION_MARKER.equals(this.value);
  }

  public static @Nullable String unbox(final @Nullable Discriminator discriminator) {
    if (discriminator == null) {
      return null;
    } else if (discriminator.migrated()) {
      return null;
    } else {
      return discriminator.value();
    }
  }

  public static <E extends Throwable> void write(
    final @Nullable Discriminator discriminator,
    final RunnableE<E> migrated,
    final Consumer1E<String, E> unmigrated
  ) throws E {
    final @Nullable String value = unbox(discriminator);
    if (value == null) {
      migrated.run();
    } else {
      unmigrated.accept(value);
    }
  }
}
