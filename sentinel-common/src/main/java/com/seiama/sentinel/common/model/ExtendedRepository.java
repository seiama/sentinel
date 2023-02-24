package com.seiama.sentinel.common.model;

import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.query.Update;
import reactor.core.publisher.Mono;

public interface ExtendedRepository<P extends AbstractPartial, M extends AbstractModel> {
  default @NotNull Mono<M> update(final @NotNull M model, final @NotNull P partial) {
    return this.update(model._id(), partial);
  }

  @NotNull Mono<M> update(final @NotNull ObjectId _id, final @NotNull P partial);

  @NotNull Mono<M> update(final @NotNull ObjectId _id, final @NotNull Update update);
}
