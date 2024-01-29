package com.seiama.sentinel.common.converter;

import discord4j.common.util.Snowflake;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@NullMarked
@WritingConverter
public class SnowflakeToStringConverter implements Converter<Snowflake, String> {
  @Override
  public String convert(final Snowflake source) {
    return source.asString();
  }
}
