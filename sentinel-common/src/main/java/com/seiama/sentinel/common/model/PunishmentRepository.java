package com.seiama.sentinel.common.model;

import discord4j.common.util.Snowflake;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface PunishmentRepository extends ReactiveMongoRepository<PunishmentModel.Complete, ObjectId>, PunishmentRepositoryCustom {
  @Deprecated // You probably want to use Punishments.findActive instead.
  Flux<PunishmentModel.Complete> findAllByGuildAndPunishedIdAndTypeAndStaleIsNotOrderByDateDesc(final Snowflake guild, final Snowflake punishedId, final PunishmentModel.Type type, final @Nullable Boolean stale);

  Flux<PunishmentModel.Complete> findAllByGuild(final Snowflake guild);

  Flux<PunishmentModel.Complete> findAllByGuildAndPunishedIdOrderByDateDesc(final Snowflake guild, final Snowflake punishedId);

  Flux<PunishmentModel.Complete> findAllByPunisherId(final Snowflake punisherId);
}
