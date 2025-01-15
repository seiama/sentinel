package com.seiama.sentinel.common.bson;

import discord4j.common.util.Snowflake;
import org.bson.BsonInvalidOperationException;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class SnowflakeCodec implements Codec<Snowflake> {
  @Override
  public Snowflake decode(final BsonReader reader, final DecoderContext decoderContext) {
    final BsonType type = reader.getCurrentBsonType();
    return switch (type) {
      case INT64 -> Snowflake.of(reader.readInt64());
      case STRING -> Snowflake.of(reader.readString());
      default -> throw new BsonInvalidOperationException(String.format("Invalid snowflake value type, found: %s", type));
    };
  }

  @Override
  public void encode(final BsonWriter writer, final Snowflake value, final EncoderContext encoderContext) {
    writer.writeString(value.asString());
  }

  @Override
  public Class<Snowflake> getEncoderClass() {
    return Snowflake.class;
  }
}
