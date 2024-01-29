package com.seiama.sentinel.controller;

import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.common.model.PunishmentRepository;
import discord4j.common.util.Snowflake;
import org.bson.types.ObjectId;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@NullMarked
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@RestController
public class PunishmentController {
  private final PunishmentRepository punishments;

  @Autowired
  public PunishmentController(final PunishmentRepository punishments) {
    this.punishments = punishments;
  }

  @CrossOrigin
  @GetMapping("/v1/punishments/{guild}")
  public Flux<PunishmentModel.Complete> punishments(final @PathVariable("guild") Snowflake guild) {
    return this.punishments.findAllByGuild(guild);
  }

  @CrossOrigin
  @GetMapping("/v1/punishments/{guild}/{id}")
  public Mono<PunishmentModel.Complete> punishmentById(final @PathVariable("guild") Snowflake guild, final @PathVariable("id") ObjectId id) {
    return this.punishments.findById(id);
  }
}
