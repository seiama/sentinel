package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

public class PunishmentRepositoryImpl extends AbstractRepository<PunishmentModel.Partial, PunishmentModel.Complete> implements PunishmentRepositoryCustom {
  @Autowired
  public PunishmentRepositoryImpl(final @Qualifier("extendedJsonMapper") ObjectMapper mapper, final ReactiveMongoTemplate template) {
    super(PunishmentModel.Complete.class, mapper, template);
  }
}
