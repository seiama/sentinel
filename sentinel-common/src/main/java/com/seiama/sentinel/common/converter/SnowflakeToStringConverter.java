package com.seiama.sentinel.common.converter;

import discord4j.common.util.Snowflake;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class SnowflakeToStringConverter implements Converter<Snowflake, String> {
  @Override
  public String convert(final Snowflake source) {
    return source.asString();
  }
}
