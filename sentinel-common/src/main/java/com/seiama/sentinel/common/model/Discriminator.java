package com.seiama.sentinel.common.model;

import com.seiama.common.functional.function.exceptional.Consumer1E;
import com.seiama.common.functional.function.exceptional.RunnableE;
import java.io.IOException;
import org.jetbrains.annotations.Nullable;

@Deprecated
public record Discriminator(
  @Nullable String value
) {
  // https://support-dev.discord.com/hc/en-us/articles/13667755828631#h_01GYA87X9QZ19H2X6PJ3M35ZDH
  private static final String TEMPORARY_MIGRATION_MARKER = "0";

  public boolean migrated() {
    return TEMPORARY_MIGRATION_MARKER.equals(this.value);
  }

  public static @Nullable String unbox(final @Nullable Discriminator discriminator) {
    if (discriminator != null && !discriminator.migrated()) {
      return discriminator.value();
    } else {
      return null;
    }
  }

  public static void write(final @Nullable Discriminator discriminator, final Consumer1E<String, IOException> present, final RunnableE<IOException> absent) throws IOException {
    if (discriminator != null && !discriminator.migrated()) {
      present.accept(discriminator.value());
    } else {
      absent.run();
    }
  }
}
