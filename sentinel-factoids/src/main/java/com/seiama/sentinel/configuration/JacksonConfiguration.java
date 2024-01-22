package com.seiama.sentinel.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.seiama.sentinel.common.jackson.CommonModule;
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
      new CommonModule()
    );
  }

  @Bean
  @Primary
  ObjectMapper defaultObjectMapper(final Jackson2ObjectMapperBuilder builder) {
    return builder.createXmlMapper(false).build();
  }
}
