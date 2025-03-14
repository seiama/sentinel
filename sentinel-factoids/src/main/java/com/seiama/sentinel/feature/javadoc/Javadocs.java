package com.seiama.sentinel.feature.javadoc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.seiama.sentinel.common.Listener;
import com.seiama.sentinel.common.model.JavadocModel;
import com.seiama.sentinel.common.model.JavadocRepository;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import java.time.Duration;
import org.jspecify.annotations.NullMarked;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@NullMarked
public class Javadocs implements Listener {
  private final JavadocRepository javadocs;
  private final Cache<String, JavadocSearch> cacheJavadocs;

  @Autowired
  public Javadocs(final JavadocRepository javadocs) {
    this.javadocs = javadocs;
    this.cacheJavadocs = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofHours(1)).build();
    javadocs.findAll().doOnEach(completeSignal -> {
      JavadocModel.Complete complete = completeSignal.get();
      JavadocSearch javaDocSearch = new JavadocSearch(complete.url());
      cacheJavadocs.put(complete.name(), javaDocSearch);
    }).subscribe();
  }

  @Override
  public Mono<Void> listen(final GatewayDiscordClient client) {
    return client.on(new ReactiveEventAdapter() {

      @Override
      public Publisher<?> onChatInputInteraction(ChatInputInteractionEvent event) {
        if (event.getCommandName().equals("random")) {
          String result = "";//event.getInteraction().getCommandInteraction().get();
          return event.reply(result);
        }
        return Mono.empty();
      }

      @Override
      public Publisher<?> onChatInputAutoCompleteInteraction(ChatInputAutoCompleteEvent event) {
        return Mono.empty();
      }
    }).then();
  }
}
