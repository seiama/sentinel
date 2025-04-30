package com.seiama.sentinel.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.seiama.sentinel.common.jackson.CommonModule;
import discord4j.common.JacksonResources;
import discord4j.common.jackson.UnknownPropertyHandler;
import discord4j.discordjson.possible.PossibleFilter;
import discord4j.discordjson.possible.PossibleModule;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@NullMarked
public class JacksonConfiguration {
  @Bean
  Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
    return builder -> builder.modules(
        new PossibleModule(),
        new JavaTimeModule(),
        new CommonModule()
      )
      .failOnUnknownProperties(false)
      .visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
      .visibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.PUBLIC_ONLY).featuresToEnable(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION).serializationInclusion(JsonInclude.Value.construct(JsonInclude.Include.CUSTOM,
        JsonInclude.Include.ALWAYS, PossibleFilter.class, null));
  }
}
