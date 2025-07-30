package com.seiama.sentinel.common.model;

import discord4j.core.object.entity.User;
import discord4j.discordjson.json.UserData;
import java.io.IOException;
import java.util.Objects;
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

  private static final Discriminator MIGRATED = new Discriminator(TEMPORARY_MIGRATION_MARKER);

  public static Discriminator of(final UserData user) {
    return of(user.discriminator());
  }

  public static Discriminator of(final User user) {
    return of(user.getDiscriminator());
  }

  public static Discriminator of(final @Nullable String value) {
    return Objects.equals(value, TEMPORARY_MIGRATION_MARKER) ? MIGRATED : new Discriminator(value);
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

  public static void write(
    final @Nullable Discriminator discriminator,
    final Writer writer
  ) throws IOException {
    final @Nullable String value = unbox(discriminator);
    if (value == null) {
      writer.writeNull();
    } else {
      writer.write(value);
    }
  }

  @NullMarked
  public interface Writer {
    void writeNull() throws IOException;

    void write(final String value) throws IOException;
  }
}
