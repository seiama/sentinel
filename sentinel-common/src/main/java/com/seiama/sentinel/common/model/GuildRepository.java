package com.seiama.sentinel.common.model;

import discord4j.common.util.Snowflake;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@NullMarked
@Repository
public interface GuildRepository extends ReactiveMongoRepository<GuildModel, ObjectId> {
  Mono<GuildModel> findByGuild(final Snowflake id);

  Mono<GuildModel> findByFeaturesPunishmentsAppealsGuild(final Snowflake id);

  Mono<GuildModel> findByGuildOrFeaturesPunishmentsAppealsGuild(final Snowflake id, final Snowflake appealGuild);
}
