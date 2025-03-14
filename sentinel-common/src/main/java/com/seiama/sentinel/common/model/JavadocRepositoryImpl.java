package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@NullMarked
public class JavadocRepositoryImpl extends AbstractRepository<JavadocModel.Partial, JavadocModel.Complete> implements JavadocRepositoryCustom {
  @Autowired
  public JavadocRepositoryImpl(final ObjectMapper mapper, final ReactiveMongoTemplate template) {
    super(JavadocModel.Complete.class, mapper, template);
  }
}
