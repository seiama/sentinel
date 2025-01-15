package com.seiama.sentinel.model;

import org.jspecify.annotations.NullMarked;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@NullMarked
@Repository
public interface TemporaryMessageLinkRepository extends ReactiveCrudRepository<TemporaryMessageLink, Long> {
  Mono<TemporaryMessageLink> findBySourceMessageId(final long sourceMessageId);

  Mono<Void> deleteAllByTargetChannelId(final long targetChannelId);
}
