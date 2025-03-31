package com.seiama.sentinel.feature.javadoc;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.seiama.sentinel.common.Listener;
import com.seiama.sentinel.common.model.JavadocModel;
import com.seiama.sentinel.common.model.JavadocRepository;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.ReactiveEventAdapter;
import discord4j.core.event.domain.interaction.ChatInputAutoCompleteEvent;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.command.ApplicationCommandOption;
import discord4j.discordjson.json.ApplicationCommandOptionChoiceData;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Stream;
import net.maisikoleni.javadoc.entities.SearchableEntity;
import net.maisikoleni.javadoc.search.RankedTrieSearchEngine;
import net.maisikoleni.javadoc.service.JavadocImpl;
import org.bson.types.ObjectId;
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
  private final LoadingCache<ObjectId, JavaDocSearchEngine> cacheSearchEngine;
  private final Cache<String, JavadocItemPartial> cacheItems;

  @Autowired
  public Javadocs(final JavadocRepository javadocs) {
    this.javadocs = javadocs;
    this.cacheSearchEngine = CacheBuilder.newBuilder()
      .expireAfterWrite(Duration.ofHours(1))
      .build(new CacheLoader<>() {
        @Override
        public JavaDocSearchEngine load(final ObjectId key) {
          return Javadocs.this.buildEngine(Objects.requireNonNull(Javadocs.this.javadocs.findById(key).block()));
        }
      });
    this.cacheItems = CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(10)).build();
    this.javadocs.findAll().map(complete -> {
      final JavaDocSearchEngine javaDocSearchEngine = this.buildEngine(complete);
      Javadocs.this.cacheSearchEngine.put(complete._id(), javaDocSearchEngine);
      return javaDocSearchEngine;
    }).subscribe();
  }

  @Override
  public Mono<Void> listen(final GatewayDiscordClient client) {
    return client.on(new ReactiveEventAdapter() {

      @Override
      public Publisher<?> onChatInputInteraction(final ChatInputInteractionEvent event) {
        return event.getInteraction()
          .getGuild()
          .flatMap(guild -> Javadocs.this.javadocs.findByGuildAndCommandId(guild.getId(), event.getCommandId()))
          .map(Javadocs.this::createEngine)
          .flatMap(javadocSearch -> {
            final String term = event.getOptions().stream().filter(option -> option.getType().equals(ApplicationCommandOption.Type.SUB_COMMAND)).findFirst().map(subCommandOption -> subCommandOption.getOption(JavadocModel.Complete.REQUEST_OPTION_JAVADOC_KEYWORD).flatMap(ApplicationCommandInteractionOption::getValue).map(ApplicationCommandInteractionOptionValue::asString).orElseThrow()).orElseThrow();
            JavadocItemPartial javadocItemPartial = Javadocs.this.cacheItems.getIfPresent(term);

            if (javadocItemPartial == null) { // the case if user make cache of element expire when ask for them
              javadocItemPartial = javadocSearch.searchEngine().search(term).findFirst().map(searchableEntity -> JavadocItemPartial.fromSearchableEntity(javadocSearch.javadoc().baseUrl(), searchableEntity)).orElse(null);
              if (javadocItemPartial == null) { // if not found any using the term not cached then just tell the user cannot find any
                return event.reply("No javadoc available for term `%s`".formatted(term));
              }
            }

            final JavadocElement javadocElement = Javadocs.this.createJavadocElement(javadocItemPartial);
            return event.reply(javadocElement.buildInteractionResponse());
          });
      }

      @Override
      public Publisher<?> onChatInputAutoCompleteInteraction(final ChatInputAutoCompleteEvent event) {
        final String term = event.getFocusedOption().getValue().orElseThrow().asString();
        if (term.isBlank()) {
          return event.respondWithSuggestions(Collections.emptyList());
        }

        return event.getInteraction()
          .getGuild()
          .flatMap(guild -> Javadocs.this.javadocs.findByGuildAndCommandId(guild.getId(), event.getCommandId()))
          .map(Javadocs.this::createEngine)
          .flatMap(jdSearch -> {
            if (event.getFocusedOption().getName().equals(JavadocModel.Complete.REQUEST_OPTION_JAVADOC_KEYWORD)) {
              final JavadocModel.Complete.ComponentType javadocComponentType = event.getOptions().stream().filter(option -> option.getType().equals(ApplicationCommandOption.Type.SUB_COMMAND)).findFirst().map(subCommandOption -> subCommandOption.getOption(JavadocModel.Complete.REQUEST_OPTION_JAVADOC_ELEMENT_TYPE).flatMap(ApplicationCommandInteractionOption::getValue).map(ApplicationCommandInteractionOptionValue::asString).map(String::toUpperCase).map(JavadocModel.Complete.ComponentType::fromString).orElse(JavadocModel.Complete.ComponentType.ALL)).orElse(JavadocModel.Complete.ComponentType.ALL);

              final Stream<? extends SearchableEntity> searchableEntities = switch (javadocComponentType) {
                case MODULE -> jdSearch.searchEngine().searchGroupedByType(term).modules();
                case PACKAGE -> jdSearch.searchEngine().searchGroupedByType(term).packages();
                case TYPE -> jdSearch.searchEngine().searchGroupedByType(term).types();
                case MEMBER -> jdSearch.searchEngine().searchGroupedByType(term).members();
                case TAG -> jdSearch.searchEngine().searchGroupedByType(term).tags();
                default -> jdSearch.searchEngine().search(term);
              };

              final Stream<JavadocItemPartial> searchableJavaDocPartial = searchableEntities.map(searchableEntity -> JavadocItemPartial.fromSearchableEntity(jdSearch.javadoc().baseUrl(), searchableEntity));

              return Flux.fromStream(searchableJavaDocPartial)
                .doOnNext(next -> Javadocs.this.cacheItems.put(String.valueOf(next.hashCode()), next)) // this is awful, but also...
                .map(item -> ApplicationCommandOptionChoiceData.builder()
                  .name(Javadocs.this.left(item.displayForChoice(), 100))
                  .value(String.valueOf(item.hashCode()))
                  .build())
                .cast(ApplicationCommandOptionChoiceData.class)
                .take(25)
                .collectList()
                .flatMap(event::respondWithSuggestions);
            } else {
              return event.respondWithSuggestions(Collections.emptyList());
            }
          });
      }

    }).then();
  }

  private JavaDocSearchEngine createEngine(final JavadocModel.Complete complete) {
    JavaDocSearchEngine javadocSearch = this.cacheSearchEngine.getIfPresent(complete._id());
    if (javadocSearch == null || !Objects.equals(javadocSearch.javadoc().baseUrl().toString(), complete.url())) {
      javadocSearch = this.buildEngine(complete);
      this.cacheSearchEngine.put(complete._id(), javadocSearch);
    }
    return javadocSearch;
  }

  private JavaDocSearchEngine buildEngine(final JavadocModel.Complete complete) {
    final JavadocImpl javadocImpl = new JavadocImpl(complete.name(), complete.name(), "", URI.create(complete.url()), this.basicJavadocIndexes);
    final IndexWithBaseUrl indexWithBaseUrl = new IndexWithBaseUrl(javadocImpl.baseUrl(), javadocImpl.index());

    return new JavaDocSearchEngine(javadocImpl, new RankedTrieSearchEngine(indexWithBaseUrl.index(), this.commonGenerator));
  }

  private JavadocElement createJavadocElement(final JavadocItemPartial javadocItemPartial) {
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
