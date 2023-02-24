package com.seiama.sentinel.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.seiama.sentinel.common.jackson.InstantExtendedJsonSerializer;
import com.seiama.sentinel.common.jackson.ObjectIdExtendedJsonSerializer;
import com.seiama.sentinel.common.jackson.ObjectIdSerializer;
import com.seiama.sentinel.common.jackson.SnowflakeSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfiguration {
  @Bean
  Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
    return builder -> builder.modules(
      new JavaTimeModule(),
      new SimpleModule()
        .addSerializer(new ObjectIdSerializer())
        .addSerializer(new SnowflakeSerializer())
    );
  }

  @Bean
  @Primary
  ObjectMapper defaultObjectMapper(final Jackson2ObjectMapperBuilder builder) {
    return builder.createXmlMapper(false).build();
  }

  @Bean("extendedJsonMapper")
  @Qualifier("extendedJsonMapper")
  ObjectMapper mapperForExtendedJson() {
    return new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .registerModule(
        new SimpleModule()
          .addSerializer(new InstantExtendedJsonSerializer())
          .addSerializer(new ObjectIdExtendedJsonSerializer())
          .addSerializer(new SnowflakeSerializer())
      );
  }
}
