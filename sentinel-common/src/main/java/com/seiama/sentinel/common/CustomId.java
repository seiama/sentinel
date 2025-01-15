package com.seiama.sentinel.common;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record CustomId(
  String type,
  String value
) {
  private static final String SEPARATOR = ":";

  public static @Nullable CustomId parse(final String string) {
    final String[] args = string.split(SEPARATOR, 2);
    if (args.length == 2) {
      return new CustomId(args[0], args[1]);
    }
    return null;
  }

  @Override
  public String toString() {
    return this.type + SEPARATOR + this.value;
  }
}
