package com.seiama.sentinel.common.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.seiama.sentinel.common.model.Discriminator;
import java.io.IOException;
import org.jspecify.annotations.NullMarked;

@NullMarked
@SuppressWarnings("deprecation")
public class DiscriminatorSerializer extends StdSerializer<Discriminator> {
  public DiscriminatorSerializer() {
    super(Discriminator.class);
  }

  @Override
  public void serialize(final Discriminator value, final JsonGenerator gen, final SerializerProvider serializers) throws IOException {
    Discriminator.write(value, gen::writeNull, gen::writeString);
  }
}
