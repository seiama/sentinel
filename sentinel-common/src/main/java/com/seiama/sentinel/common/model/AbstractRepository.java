package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public abstract class AbstractRepository<P extends AbstractPartial, M extends AbstractModel> implements ExtendedRepository<P, M> {
  protected final Class<M> model;
  protected final ObjectMapper mapper;
  protected final ReactiveMongoTemplate template;

  protected AbstractRepository(final Class<M> model, final ObjectMapper mapper, final ReactiveMongoTemplate template) {
    this.model = model;
    this.mapper = mapper;
    this.template = template;
  }

  @Override
  public @NotNull Mono<M> update(final @NotNull ObjectId _id, @NotNull final P partial) {
    return AbstractPartial.toBson(this.mapper, partial)
      .flatMap(bson -> this.update(_id, Update.fromDocument(new Document("$set", bson))));
  }

  @Override
  public @NotNull Mono<M> update(final @NotNull ObjectId _id, final @NotNull Update update) {
    return this.template.updateFirst(
      Query.query(Criteria.where(AbstractModel._ID).is(_id)),
      update,
      this.model
    ).flatMap(result -> this.template.findById(_id, this.model));
  }

  @Override
  public @NotNull Mono<M> refresh(final @NotNull M that) {
    return this.template.findById(that._id(), this.model);
  }
}
