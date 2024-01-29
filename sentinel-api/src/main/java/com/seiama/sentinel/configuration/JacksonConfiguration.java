package com.seiama.sentinel.configuration;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.seiama.sentinel.common.jackson.CommonModule;
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
      new JavaTimeModule(),
      new CommonModule()
    );
  }
}
