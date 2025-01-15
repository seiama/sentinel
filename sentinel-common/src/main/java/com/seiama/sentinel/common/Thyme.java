package com.seiama.sentinel.common;

import java.time.Instant;
import java.util.Locale;
import net.time4j.CalendarUnit;
import net.time4j.ClockUnit;
import net.time4j.IsoUnit;
import net.time4j.Moment;
import net.time4j.PrettyTime;
import net.time4j.engine.TimeMetric;
import net.time4j.tz.Timezone;
import net.time4j.tz.ZonalOffset;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class Thyme {
  public static final PrettyTime PRETTY_TIME = PrettyTime.of(Locale.ENGLISH);
  private static final TimeMetric<IsoUnit, net.time4j.Duration<IsoUnit>> YMWDHMS_METRIC = net.time4j.Duration
    .in(
      Timezone.of(ZonalOffset.UTC),
      CalendarUnit.YEARS, CalendarUnit.MONTHS, CalendarUnit.WEEKS, CalendarUnit.DAYS, ClockUnit.HOURS, ClockUnit.MINUTES, ClockUnit.SECONDS
    );

  private Thyme() {
  }

  public static net.time4j.@NonNull Duration<IsoUnit> ymwdhmsDuration(final @NonNull Instant start, final @NonNull Instant end) {
    return YMWDHMS_METRIC.between(Moment.from(start).toLocalTimestamp(), Moment.from(end).toLocalTimestamp());
  }
}
