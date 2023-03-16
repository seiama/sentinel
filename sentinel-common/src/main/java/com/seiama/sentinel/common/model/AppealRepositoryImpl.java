package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

public class AppealRepositoryImpl extends AbstractRepository<AppealModel.Partial, AppealModel.Complete> implements AppealRepositoryCustom {
  @Autowired
  public AppealRepositoryImpl(final ObjectMapper mapper, final ReactiveMongoTemplate template) {
    super(AppealModel.Complete.class, mapper, template);
  }
}
