package com.seiama.sentinel.common.model;

import discord4j.common.util.Snowflake;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AppealRepository extends ReactiveMongoRepository<AppealModel.Complete, ObjectId>, AppealRepositoryCustom {
  Flux<AppealModel.Complete> findAllByResultIsNull();

  Mono<AppealModel.Complete> findByAppealChannel(final Snowflake appealChannel);

  Mono<AppealModel.Complete> findByAppealThread(final Snowflake appealThread);

  Mono<AppealModel.Complete> findByGuildAndUserAndResultIsNull(final Snowflake guild, final Snowflake user);

  Mono<AppealModel.Complete> findByAppealDiscussionThreadAndResultIsNull(final Snowflake appealDiscussionThread);
}
