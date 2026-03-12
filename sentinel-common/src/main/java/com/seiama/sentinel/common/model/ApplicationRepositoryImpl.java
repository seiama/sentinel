package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@NullMarked
public class ApplicationRepositoryImpl extends AbstractRepository<ApplicationModel.Partial, ApplicationModel.Complete> implements ApplicationRepositoryCustom {
  @Autowired
  public ApplicationRepositoryImpl(final ObjectMapper mapper, final ReactiveMongoTemplate template) {
    super(ApplicationModel.Complete.class, mapper, template);
  }
}
