package com.seiama.sentinel.common.model;

import discord4j.common.util.Snowflake;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface AppealRepository extends ReactiveMongoRepository<PunishmentAppealModel.Complete, ObjectId>, AppealRepositoryCustom {
  Flux<PunishmentAppealModel.Complete> findAllByResultIsNull();

  Mono<PunishmentAppealModel.Complete> findByAppealChannel(final Snowflake appealChannel);

  Mono<PunishmentAppealModel.Complete> findByAppealThread(final Snowflake appealThread);

  Mono<PunishmentAppealModel.Complete> findByGuildAndUserAndResultIsNull(final Snowflake guild, final Snowflake user);

  Mono<PunishmentAppealModel.Complete> findByAppealDiscussionThreadAndResultIsNull(final Snowflake appealDiscussionThread);
}
