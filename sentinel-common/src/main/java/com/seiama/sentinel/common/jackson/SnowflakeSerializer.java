package com.seiama.sentinel.common.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import discord4j.common.util.Snowflake;
import java.io.IOException;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class SnowflakeSerializer extends StdSerializer<Snowflake> {
  public SnowflakeSerializer() {
    super(Snowflake.class);
  }

  @Override
  public void serialize(final Snowflake value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
    gen.writeString(value.asString());
  }
}
