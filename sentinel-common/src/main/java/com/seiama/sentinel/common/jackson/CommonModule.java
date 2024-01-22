package com.seiama.sentinel.common.jackson;

import com.fasterxml.jackson.databind.module.SimpleModule;

public final class CommonModule extends SimpleModule {
  public CommonModule() {
    this.addSerializer(new DiscriminatorSerializer());
    this.addSerializer(new ObjectIdSerializer());
    this.addSerializer(new SnowflakeSerializer());
  }
}
