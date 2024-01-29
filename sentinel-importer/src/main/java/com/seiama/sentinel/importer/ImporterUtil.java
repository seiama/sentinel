package com.seiama.sentinel.importer;

import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ImporterUtil {
  private ImporterUtil() {
  }

  public static boolean equalsNotNull(final @Nullable Object a, final @Nullable Object b) {
    if (a == null) return false;
    if (b == null) return false;
    return Objects.equals(a, b);
  }

  public static Predicate<@Nullable String> matchStartEnd(final String start, final String end) {
    return string -> string != null && string.startsWith(start) && string.endsWith(end);
  }

  public static <T> boolean matches(final T a, final T b, final List<BiPredicate<T, T>> predicates) {
    for (final BiPredicate<T, T> predicate : predicates) {
      if (!predicate.test(a, b)) {
        return false;
      }
    }
    return true;
  }
}
