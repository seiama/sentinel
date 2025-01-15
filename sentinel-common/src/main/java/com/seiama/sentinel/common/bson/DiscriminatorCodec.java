package com.seiama.sentinel.common.bson;

import com.seiama.sentinel.common.model.Discriminator;
import java.io.IOException;
import org.bson.BsonInvalidOperationException;
import org.bson.BsonReader;
import org.bson.BsonType;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;

@SuppressWarnings("deprecation")
public final class DiscriminatorCodec implements Codec<Discriminator> {
  @Override
  public Discriminator decode(final BsonReader reader, final DecoderContext decoderContext) {
    final BsonType type = reader.getCurrentBsonType();
    return switch (type) {
      case STRING -> new Discriminator(reader.readString());
      case NULL -> null;
      default -> throw new BsonInvalidOperationException(String.format("Invalid discriminator value type, found: %s", type));
    };
  }

  @Override
  public void encode(final BsonWriter writer, final Discriminator value, final EncoderContext encoderContext) {
    try {
      Discriminator.write(value, writer::writeString, writer::writeNull);
    } catch (final IOException e) {
      throw new BsonInvalidOperationException("This should never happen.", e);
    }
  }

  @Override
  public Class<Discriminator> getEncoderClass() {
    return Discriminator.class;
  }
}
