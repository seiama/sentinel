package com.seiama.sentinel.feature.punishment;

import java.time.Duration;
import org.jspecify.annotations.NullMarked;

@NullMarked
public enum TimeoutDuration {
  SECONDS_60("60 secs", Duration.ofSeconds(60)),
  MINUTES_5("5 mins", Duration.ofMinutes(5)),
  MINUTES_10("10 mins", Duration.ofMinutes(10)),
  HOURS_1("1 hour", Duration.ofHours(1)),
  DAYS_1("1 day", Duration.ofDays(1)),
  WEEKS_1("1 week", Duration.ofDays(7)),
  MONTHS_1("1 month", Duration.ofDays(28));

  private final String description;
  private final Duration duration;

  TimeoutDuration(final String description, final Duration duration) {
    this.description = description;
    this.duration = duration;
  }

  public String description() {
    return this.description;
  }

  public Duration duration() {
    return this.duration;
  }
}
