package com.seiama.sentinel.common.model;

import discord4j.common.util.Snowflake;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface JavadocRepository extends ReactiveMongoRepository<JavadocModel.Complete, ObjectId>, JavadocRepositoryCustom {
  Flux<JavadocModel.Complete> findAllByGuild(final Snowflake guild);

  Mono<JavadocModel.Complete> findByGuildAndName(final Snowflake guild, final String name);
}
