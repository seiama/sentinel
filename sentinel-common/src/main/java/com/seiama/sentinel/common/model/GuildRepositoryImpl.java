package com.seiama.sentinel.common.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

@NullMarked
public class GuildRepositoryImpl extends AbstractRepository<GuildModel.Partial, GuildModel.Complete> implements GuildRepositoryCustom {
  @Autowired
  public GuildRepositoryImpl(final ObjectMapper mapper, final ReactiveMongoTemplate template) {
    super(GuildModel.Complete.class, mapper, template);
  }
}
