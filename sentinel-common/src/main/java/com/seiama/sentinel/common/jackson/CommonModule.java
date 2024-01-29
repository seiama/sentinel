package com.seiama.sentinel.common.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class CommonModule extends SimpleModule {
  public CommonModule() {
    this.addSerializer(new DiscriminatorSerializer());
    this.addSerializer(new ObjectIdSerializer());
    this.addSerializer(new SnowflakeSerializer());
  }
}
