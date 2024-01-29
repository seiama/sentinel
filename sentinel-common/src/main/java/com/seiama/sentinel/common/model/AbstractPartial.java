package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.jspecify.annotations.NullMarked;
import reactor.core.publisher.Mono;

@NullMarked
public interface AbstractPartial {
  static Mono<Bson> toBson(final ObjectMapper mapper, final AbstractPartial partial) {
    try {
      return Mono.just(Document.parse(mapper.writeValueAsString(partial)));
    } catch (final JsonProcessingException e) {
      return Mono.error(e);
    }
  }
}
