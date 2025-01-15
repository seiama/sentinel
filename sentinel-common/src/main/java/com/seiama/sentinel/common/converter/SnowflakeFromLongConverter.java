package com.seiama.sentinel.common.converter;

import discord4j.common.util.Snowflake;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@Component
@ReadingConverter
public class SnowflakeFromLongConverter implements Converter<Long, Snowflake> {
  @Override
  public Snowflake convert(final Long source) {
    return Snowflake.of(source);
  }
}
