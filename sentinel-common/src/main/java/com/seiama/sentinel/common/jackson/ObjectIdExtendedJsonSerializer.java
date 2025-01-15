package com.seiama.sentinel.common.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.bson.types.ObjectId;

public class ObjectIdExtendedJsonSerializer extends StdSerializer<ObjectId> {
  public ObjectIdExtendedJsonSerializer() {
    super(ObjectId.class);
  }

  @Override
  public void serialize(final ObjectId value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
    gen.writeStartObject();
    gen.writeStringField("$oid", value.toHexString());
    gen.writeEndObject();
  }
}
