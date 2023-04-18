package com.seiama.sentinel.controller;

import com.seiama.sentinel.common.model.FactoidModel;
import com.seiama.sentinel.common.model.FactoidRepository;
import discord4j.common.util.Snowflake;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class FactoidController {
  private final FactoidRepository factoids;

  @Autowired
  public FactoidController(FactoidRepository factoids) {
    this.factoids = factoids;
  }

  @CrossOrigin
  @GetMapping("/v1/factoids/{guild}")
  public Flux<FactoidModel.Complete> factoids(final @PathVariable("guild") Snowflake guild) {
    return this.factoids.findAllByGuild(guild);
  }

  @CrossOrigin
  @GetMapping("/v1/factoids/{guild}/{id}")
  public Mono<FactoidModel.Complete> factoidsById(final @PathVariable("guild") Snowflake guild, final @PathVariable("id") ObjectId id) {
    return this.factoids.findById(id);
  }
}
