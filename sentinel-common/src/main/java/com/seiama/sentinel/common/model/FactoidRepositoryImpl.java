package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@NullMarked
public class FactoidRepositoryImpl extends AbstractRepository<FactoidModel.Partial, FactoidModel.Complete> implements FactoidRepositoryCustom {
  @Autowired
  public FactoidRepositoryImpl(final ObjectMapper mapper, final ReactiveMongoTemplate template) {
    super(FactoidModel.Complete.class, mapper, template);
  }
}
