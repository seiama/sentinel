package com.seiama.sentinel.common.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.time.Instant;

public class InstantExtendedJsonSerializer extends StdSerializer<Instant> {
  public InstantExtendedJsonSerializer() {
    super(Instant.class);
  }

  @Override
  public void serialize(final Instant value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
    gen.writeStartObject();
    gen.writeStringField("$date", value.toString());
    gen.writeEndObject();
  }
}
