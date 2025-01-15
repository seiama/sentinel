package com.seiama.sentinel.feature.factoid;

import com.seiama.sentinel.common.Listener;
import com.seiama.sentinel.common.model.FactoidModel;
import com.seiama.sentinel.common.model.FactoidRepository;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@NullMarked
public class Factoids implements Listener {
  private final FactoidRepository factoids;

  @Autowired
  public Factoids(final FactoidRepository factoids) {
    this.factoids = factoids;
  }

  @Override
  public Mono<Void> listen(final GatewayDiscordClient client) {
    return client.on(ChatInputInteractionEvent.class, event -> {
      return event.getInteraction()
        .getGuild()
        .flatMap(guild -> this.factoids.findByGuildAndName(guild.getId(), event.getCommandName()))
        .map(FactoidModel.Complete::response)
        .flatMap(response -> response.decorate(event.reply()));
    }).then();
  }
}
