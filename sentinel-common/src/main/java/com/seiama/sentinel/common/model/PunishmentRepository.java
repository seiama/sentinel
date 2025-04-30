package com.seiama.sentinel.common.model;

import discord4j.common.util.Snowflake;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@NullMarked
@Repository
public interface PunishmentRepository extends AbstractRepository<PunishmentModel>, ReactiveMongoRepository<PunishmentModel, ObjectId> {
  @Deprecated // You probably want to use Punishments.findActive instead.
  Flux<PunishmentModel> findAllByGuildAndPunishedIdAndTypeAndStaleIsNotOrderByDateDesc(final Snowflake guild, final Snowflake punishedId, final PunishmentModel.Type type, final @Nullable Boolean stale);

  Flux<PunishmentModel> findAllByGuild(final Snowflake guild);

  Flux<PunishmentModel> findAllByGuildAndPunishedIdOrderByDateDesc(final Snowflake guild, final Snowflake punishedId);

  Flux<PunishmentModel> findAllByPunisherId(final Snowflake punisherId);
}
