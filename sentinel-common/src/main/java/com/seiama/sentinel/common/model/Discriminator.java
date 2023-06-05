package com.seiama.sentinel.common.model;

import com.seiama.common.functional.function.exceptional.Consumer1E;
import com.seiama.common.functional.function.exceptional.RunnableE;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

@Deprecated
public record Discriminator(
  @Nullable String value
) {
  // https://support-dev.discord.com/hc/en-us/articles/13667755828631#h_01GYA87X9QZ19H2X6PJ3M35ZDH
  @VisibleForTesting
  public static final String TEMPORARY_MIGRATION_MARKER = "0";

  public boolean migrated() {
    return TEMPORARY_MIGRATION_MARKER.equals(this.value);
  }

  public static @Nullable String unbox(final @Nullable Discriminator discriminator) {
    if (discriminator == null || discriminator.migrated()) {
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
    if (discriminator == null || discriminator.migrated()) {
      migrated.run();
    } else {
      unmigrated.accept(discriminator.value());
    }
  }
}
