package com.seiama.sentinel.model;

import discord4j.common.util.Snowflake;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface TemporaryMessageLinkRepository extends ReactiveCrudRepository<TemporaryMessageLink, Long> {
  Mono<TemporaryMessageLink> findBySourceMessageId(final String sourceMessageId);

  Mono<Void> deleteAllByTargetChannelId(final String targetChannelId);
}
