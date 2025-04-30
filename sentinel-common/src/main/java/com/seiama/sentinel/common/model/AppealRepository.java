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
public interface AppealRepository extends AbstractRepository<AppealModel>, ReactiveMongoRepository<AppealModel, ObjectId> {
  Flux<AppealModel> findAllByResultIsNull();

  Mono<AppealModel> findByAppealChannel(final Snowflake appealChannel);

  Mono<AppealModel> findByAppealThread(final Snowflake appealThread);

  Mono<AppealModel> findByGuildAndUserAndResultIsNull(final Snowflake guild, final Snowflake user);

  Mono<AppealModel> findByAppealDiscussionThreadAndResultIsNull(final Snowflake appealDiscussionThread);
}
