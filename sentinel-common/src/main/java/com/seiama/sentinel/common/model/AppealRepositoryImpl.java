package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

public class AppealRepositoryImpl extends AbstractRepository<PunishmentAppealModel.Partial, PunishmentAppealModel.Complete> implements AppealRepositoryCustom {
  @Autowired
  public AppealRepositoryImpl(final @Qualifier("extendedJsonMapper") ObjectMapper mapper, final ReactiveMongoTemplate template) {
    super(PunishmentAppealModel.Complete.class, mapper, template);
  }
}
