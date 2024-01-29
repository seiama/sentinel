package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@NullMarked
public class ModMailRepositoryImpl extends AbstractRepository<ModMailModel.Partial, ModMailModel.Complete> implements ModMailRepositoryCustom {
  @Autowired
  public ModMailRepositoryImpl(final ObjectMapper mapper, final ReactiveMongoTemplate template) {
    super(ModMailModel.Complete.class, mapper, template);
  }
}
