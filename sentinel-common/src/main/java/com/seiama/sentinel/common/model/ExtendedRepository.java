package com.seiama.sentinel.common.model;

import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Mono;

@NullMarked
public interface ExtendedRepository<P extends AbstractPartial, M extends AbstractModel> {
  default Mono<M> update(final M model, final P partial) {
    return this.update(model._id(), partial);
  }

  Mono<M> update(final ObjectId _id, final P partial);

  Mono<M> update(final ObjectId _id, final Update update);

  Mono<M> refresh(final M that);
}
