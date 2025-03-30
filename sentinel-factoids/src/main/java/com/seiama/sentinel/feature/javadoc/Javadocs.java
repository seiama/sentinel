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
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.stream.Stream;
import net.maisikoleni.javadoc.entities.SearchableEntity;
import net.maisikoleni.javadoc.search.RankedTrieSearchEngine;
import net.maisikoleni.javadoc.service.JavadocImpl;
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
  private final RankedTrieSearchEngine.RankedConcurrentTrieGenerator commonGenerator = RankedTrieSearchEngine.RankedConcurrentTrieGenerator.of();
  private final BasicJavadocIndexes basicJavadocIndexes = new BasicJavadocIndexes();
  private final JavadocRepository javadocs;
  private final Cache<String, JavaDocSearchEngine> cacheSearchEngine;
  private final Cache<String, JavadocItemPartial> cacheItems;

  @Autowired
  public Javadocs(final JavadocRepository javadocs) {
    this.javadocs = javadocs;
    this.cacheSearchEngine = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofHours(1)).build();
    this.cacheItems = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).build();
    this.javadocs.findAll().map(complete -> {
      JavaDocSearchEngine javaDocSearchEngine = this.buildEngine(complete);
      cacheSearchEngine.put(complete.name(), javaDocSearchEngine);
      return javaDocSearchEngine;
    }).subscribe();
  }

  @Override
  public Mono<Void> listen(final GatewayDiscordClient client) {
    return client.on(new ReactiveEventAdapter() {

      @Override
      public Publisher<?> onChatInputInteraction(ChatInputInteractionEvent event) {
        return event.getInteraction()
          .getGuild()
          .flatMap(guild -> javadocs.findByGuildAndCommandId(guild.getId(), event.getCommandId()))
          .map(complete -> {
            JavaDocSearchEngine javadocSearch = cacheSearchEngine.getIfPresent(complete.name());
            if (javadocSearch == null) {
              javadocSearch = buildEngine(complete);
              cacheSearchEngine.put(complete.name(), javadocSearch);
            }
            return javadocSearch;
          })
          .map(javadocSearch -> {
            final String term = event.getOption(JavadocModel.Complete.REQUEST_OPTION_JAVADOC_KEYWORD).flatMap(ApplicationCommandInteractionOption::getValue).map(ApplicationCommandInteractionOptionValue::asString).orElseThrow();
            JavadocItemPartial javadocItemPartial = cacheItems.getIfPresent(term);
            assert javadocItemPartial != null; // Cache still has this
            return getJavadocElement(javadocItemPartial);
          })
          .flatMap(javadocItem -> event.reply(javadocItem.buildInteractionResponse()));
      }

      @Override
      public Publisher<?> onChatInputAutoCompleteInteraction(ChatInputAutoCompleteEvent event) {
        final String term = event.getFocusedOption().getValue().orElseThrow().asString();
        if (term.isBlank()) {
          return event.respondWithSuggestions(Collections.emptyList());
        }

        if (event.getFocusedOption().getName().equals(JavadocModel.Complete.REQUEST_OPTION_JAVADOC_ELEMENT_TYPE)) {
          return Flux.fromStream(Arrays.stream(JavadocComponentType.values()))
            .filter(javadocComponentType -> javadocComponentType.name().toLowerCase(Locale.ROOT).contains(term))
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
          final JavadocComponentType javadocComponentType = event.getOption(JavadocModel.Complete.REQUEST_OPTION_JAVADOC_ELEMENT_TYPE).flatMap(ApplicationCommandInteractionOption::getValue).map(ApplicationCommandInteractionOptionValue::asString).map(String::toUpperCase).map(JavadocComponentType::fromString).orElse(JavadocComponentType.UNKNOW);

          JavaDocSearchEngine jdSearch = cacheSearchEngine.getIfPresent(javadocName);
          if (jdSearch == null) {
            return event.respondWithSuggestions(Collections.emptyList());
          }

          Stream<? extends SearchableEntity> searchableEntities = switch (javadocComponentType) {
            case MODULE -> jdSearch.searchEngine().searchGroupedByType(term).modules();
            case PACKAGE -> jdSearch.searchEngine().searchGroupedByType(term).packages();
            case TYPE -> jdSearch.searchEngine().searchGroupedByType(term).types();
            case MEMBER -> jdSearch.searchEngine().searchGroupedByType(term).members();
            case TAG -> jdSearch.searchEngine().searchGroupedByType(term).tags();
            default -> jdSearch.searchEngine().search(term);
          };

          Stream<JavadocItemPartial> searchableJavaDocPartial = searchableEntities.map(searchableEntity -> JavadocItemPartial.fromSearchableEntity(jdSearch.javadoc().baseUrl(), searchableEntity));

          return Flux.fromStream(searchableJavaDocPartial)
            .doOnNext(next -> cacheItems.put(String.valueOf(next.hashCode()), next)) // this is awful, but also...
            .map(item -> ApplicationCommandOptionChoiceData.builder()
              .name(left(item.displayName(), 100))
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

  private JavaDocSearchEngine buildEngine(JavadocModel.Complete complete) {
    JavadocImpl javadocImpl = new JavadocImpl(complete.name(), complete.name(), "", URI.create(complete.url()), this.basicJavadocIndexes);
    IndexWithBaseUrl indexWithBaseUrl = new IndexWithBaseUrl(javadocImpl.baseUrl(), javadocImpl.index());

    return new JavaDocSearchEngine(javadocImpl, new RankedTrieSearchEngine(indexWithBaseUrl.index(), this.commonGenerator));
  }

  private JavadocElement getJavadocElement(final JavadocItemPartial javadocItemPartial) {
    return new JavadocElement(javadocItemPartial);
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
