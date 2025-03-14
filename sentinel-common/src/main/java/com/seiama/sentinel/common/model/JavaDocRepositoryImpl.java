package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@NullMarked
public class JavaDocRepositoryImpl extends AbstractRepository<JavadocModel.Partial, JavadocModel.Complete> implements JavaDocRepositoryCustom {
  @Autowired
  public JavaDocRepositoryImpl(final ObjectMapper mapper, final ReactiveMongoTemplate template) {
    super(JavadocModel.Complete.class, mapper, template);
  }
}
