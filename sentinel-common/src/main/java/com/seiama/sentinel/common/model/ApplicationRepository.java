package com.seiama.sentinel.common.model;

import discord4j.common.util.Snowflake;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@NullMarked
@Repository
public interface ApplicationRepository extends ReactiveMongoRepository<ApplicationModel.Complete, ObjectId>, ApplicationRepositoryCustom {

  Mono<ApplicationModel.Complete> findByGuildAndUserOrderByDate(final Snowflake guild, final Snowflake user);
}
