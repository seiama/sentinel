package com.seiama.sentinel.common;

import org.jspecify.annotations.NullMarked;

@FunctionalInterface
@NullMarked
public interface IsEnabled {
  boolean enabled();
}
