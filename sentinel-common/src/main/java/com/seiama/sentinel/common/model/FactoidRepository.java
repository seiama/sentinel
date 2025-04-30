package com.seiama.sentinel.common.model;

import discord4j.common.util.Snowflake;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@NullMarked
@Repository
public interface FactoidRepository extends AbstractRepository<FactoidModel>, ReactiveMongoRepository<FactoidModel, ObjectId> {
  Flux<FactoidModel> findAllByGuild(final Snowflake guild);

  Mono<FactoidModel> findByGuildAndName(final Snowflake guild, final String name);
}
