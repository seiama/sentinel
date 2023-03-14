package com.seiama.sentinel.common.model;

import discord4j.common.util.Snowflake;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface FactoidRepository extends ReactiveMongoRepository<FactoidModel.Complete, ObjectId>, FactoidRepositoryCustom {
  Flux<FactoidModel.Complete> findAllByGuild(final Snowflake guild);

  Mono<FactoidModel.Complete> findByGuildAndName(final Snowflake guild, final String name);
}
