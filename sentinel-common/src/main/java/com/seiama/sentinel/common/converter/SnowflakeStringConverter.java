package com.seiama.sentinel.common.converter;

import discord4j.common.util.Snowflake;
import org.springframework.core.convert.converter.Converter;

public class SnowflakeStringConverter implements Converter<Snowflake, String> {
  @Override
  public String convert(final Snowflake source) {
    return source.asString();
  }
}
