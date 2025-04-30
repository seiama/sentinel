package com.seiama.sentinel.common.model;

import java.util.function.Consumer;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

@NullMarked
public interface AbstractRepository<M extends AbstractModel> extends ReactiveMongoRepository<M, ObjectId> {
  default Mono<M> update(final M model, final Consumer<M> consumer) {
    consumer.accept(model);
    return this.save(model);
  }

  default Mono<M> refresh(final M that) {
    return this.findById(that._id());
  }
}
