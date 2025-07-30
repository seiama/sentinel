package com.seiama.sentinel.common.model;

import discord4j.common.util.Snowflake;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@NullMarked
@Repository
public interface GuildRepository extends ReactiveMongoRepository<GuildModel.Complete, ObjectId> {
  Mono<GuildModel.Complete> findByGuild(final Snowflake id);

  Mono<GuildModel.Complete> findByFeaturesPunishmentsAppealsGuild(final Snowflake id);

  Mono<GuildModel.Complete> findByGuildOrFeaturesPunishmentsAppealsGuild(final Snowflake id, final Snowflake appealGuild);
}
