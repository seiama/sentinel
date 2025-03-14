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
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.core.spec.InteractionApplicationCommandCallbackSpec;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@NullMarked
public class Javadocs implements Listener {
  private final JavadocRepository javadocs;
  private final Cache<String, JavadocSearch> cacheJavadocs;
  private final Cache<String, JavadocItemPartial> cacheItems;

  @Autowired
  public Javadocs(final JavadocRepository javadocs) {
    this.javadocs = javadocs;
    this.cacheJavadocs = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofHours(1)).build();
    this.cacheItems = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).build();
    // TODO: This is bad i just make this for test, need move to a valid workflow for preload
    javadocs.findAll().switchIfEmpty(Flux.empty()).collectList().blockOptional().ifPresent(list -> {
      list.forEach(complete -> {
        JavadocSearch javaDocSearch = new JavadocSearch(complete.url());
        cacheJavadocs.put(complete.name(), javaDocSearch);
      });
    });
  }

  @Override
  public Mono<Void> listen(final GatewayDiscordClient client) {
    return client.on(new ReactiveEventAdapter() {

      @Override
      public Publisher<?> onChatInputInteraction(ChatInputInteractionEvent event) {
        return event.getInteraction()
          .getGuild()
          .flatMap(guild -> javadocs.findByGuildAndCommandId(guild.getId(), event.getCommandId()))
          .mapNotNull(complete -> Objects.requireNonNull(cacheJavadocs.getIfPresent(complete.name())))
          // TODO: Handle cases where the cache not has the element and need "find" again
          .switchIfEmpty(Mono.empty())
          .map(javadocSearch -> {
            final JavadocElementType javadocElementType = event.getOption(JavadocModel.Complete.REQUEST_OPTION_JAVADOC_ELEMENT_TYPE).flatMap(ApplicationCommandInteractionOption::getValue).map(ApplicationCommandInteractionOptionValue::asString).map(String::toUpperCase).map(JavadocElementType::valueOf).orElse(null);
            final String term = event.getOption(JavadocModel.Complete.REQUEST_OPTION_JAVADOC_KEYWORD).flatMap(ApplicationCommandInteractionOption::getValue).map(ApplicationCommandInteractionOptionValue::asString).orElseThrow();
            JavadocItemPartial javadocItemPartial = cacheItems.getIfPresent(term); // TODO: This too need check if still exists
            return javadocSearch.getJavadocItem(javadocItemPartial);
          })
          .flatMap(javadocItem -> {
            InteractionApplicationCommandCallbackSpec.Builder builder = InteractionApplicationCommandCallbackSpec.builder();
            if (javadocItem.deprecated()) {
              builder.content("This element is deprecated: ```" + (javadocItem.deprecatedMessage().isBlank() ? "no deprecation message set." : javadocItem.deprecatedMessage()) + "```");
            }
            builder.embeds(EmbedCreateSpec.builder().description(javadocItem.url()).build());
            return event.reply(builder.build());
          });
      }

      @Override
      public Publisher<?> onChatInputAutoCompleteInteraction(ChatInputAutoCompleteEvent event) {
        final String term = event.getFocusedOption().getValue().orElseThrow().asString();
        if (term.isBlank()) {
          return event.respondWithSuggestions(Collections.emptyList());
        }

        if (event.getFocusedOption().getName().equals(JavadocModel.Complete.REQUEST_OPTION_JAVADOC_ELEMENT_TYPE)) {
          return Flux.fromStream(Arrays.stream(JavadocElementType.values()))
            .filter(javadocElementType -> javadocElementType.name().toLowerCase().contains(term))
            .map(item -> ApplicationCommandOptionChoiceData.builder()
              .name(left(item.name(), 100))
              .value(item.name())
              .build())
            .cast(ApplicationCommandOptionChoiceData.class)
            .take(25)
            .collectList()
            .flatMap(event::respondWithSuggestions);
        } else if (event.getFocusedOption().getName().equals(JavadocModel.Complete.REQUEST_OPTION_JAVADOC_KEYWORD)) {
          // TODO: Maybe this can be improvement?
          final String javadocName = event.getCommandName().replace("javadoc-", "");
          final JavadocElementType javadocElementType = event.getOption(JavadocModel.Complete.REQUEST_OPTION_JAVADOC_ELEMENT_TYPE).flatMap(ApplicationCommandInteractionOption::getValue).map(ApplicationCommandInteractionOptionValue::asString).map(String::toUpperCase).map(JavadocElementType::fromString).orElse(JavadocElementType.UNKNOW);

          System.out.println(JavadocModel.Complete.REQUEST_OPTION_JAVADOC_KEYWORD + " -> " + javadocName);

          JavadocSearch jdSearch = cacheJavadocs.getIfPresent(javadocName);
          if (jdSearch == null) {
            return event.respondWithSuggestions(Collections.emptyList());
          }

          return Flux.fromIterable(jdSearch.search(term, javadocElementType))
            .doOnNext(next -> cacheItems.put(String.valueOf(next.hashCode()), next)) // this is awful, but also...
            .map(item -> ApplicationCommandOptionChoiceData.builder()
              .name(left(item.nameSuggest(), 100))
              .value(String.valueOf(item.hashCode()))
              .build())
            .cast(ApplicationCommandOptionChoiceData.class)
            .take(25)
            .collectList()
            .flatMap(event::respondWithSuggestions);
        }
        return event.respondWithSuggestions(Collections.emptyList());
      }

    }).then();
  }

  private String left(final @Nullable String string, final int length) {
    if (string == null || length <= 0) {
      return "";
    }

    if (string.length() <= length) {
      return string;
    }

    return string.substring(0, length);
  }
}
