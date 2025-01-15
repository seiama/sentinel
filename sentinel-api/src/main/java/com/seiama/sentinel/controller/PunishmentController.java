package com.seiama.sentinel.controller;

import com.seiama.sentinel.common.model.PunishmentModel;
import com.seiama.sentinel.common.model.PunishmentRepository;
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
public class PunishmentController {
  private final PunishmentRepository punishments;

  @Autowired
  public PunishmentController(final PunishmentRepository punishments) {
    this.punishments = punishments;
  }

  @CrossOrigin
  @GetMapping("/v1/punishments")
  public Flux<PunishmentModel.Complete> punishments() {
    return this.punishments.findAll();
  }

  @CrossOrigin
  @GetMapping("/v1/punishment/{id}")
  public Mono<PunishmentModel.Complete> punishmentById(final @PathVariable("id") String id) {
    return this.punishments.findById(new ObjectId(id));
  }
}
