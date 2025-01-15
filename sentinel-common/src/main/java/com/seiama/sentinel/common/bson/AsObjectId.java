package com.seiama.sentinel.common.bson;

import java.util.function.BiConsumer;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import reactor.core.publisher.SynchronousSink;

@NullMarked
public final class AsObjectId implements BiConsumer<String, SynchronousSink<ObjectId>> {
  public static final AsObjectId INSTANCE = new AsObjectId();

  @Override
  public void accept(final String string, final SynchronousSink<ObjectId> sink) {
    try {
      final ObjectId id = new ObjectId(string);
      sink.next(id);
    } catch (final IllegalArgumentException e) {
      sink.error(e);
    }
  }
}
