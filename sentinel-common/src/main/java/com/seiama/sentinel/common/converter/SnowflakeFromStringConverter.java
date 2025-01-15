package com.seiama.sentinel.common.converter;

import discord4j.common.util.Snowflake;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SnowflakeFromStringConverter implements AttributeConverter<Snowflake, String> {
  @Override
  public String convertToDatabaseColumn(final Snowflake attribute) {
    return attribute.asString();
  }

  @Override
  public Snowflake convertToEntityAttribute(final String dbData) {
    return Snowflake.of(dbData);
  }
}
