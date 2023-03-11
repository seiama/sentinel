package com.seiama.sentinel.common.model;

import discord4j.common.util.Snowflake;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PunishmentRepository extends ReactiveMongoRepository<PunishmentModel.Complete, ObjectId>, PunishmentRepositoryCustom {
  Mono<PunishmentModel.Complete> findByPunishedIdAndStaleIsNotOrderByDateDesc(final Snowflake punishedId, final @Nullable Boolean stale);

  Flux<PunishmentModel.Complete> findAllByPunishedId(final Snowflake punishedId);

  Flux<PunishmentModel.Complete> findAllByPunisherId(final Snowflake punisherId);
}
