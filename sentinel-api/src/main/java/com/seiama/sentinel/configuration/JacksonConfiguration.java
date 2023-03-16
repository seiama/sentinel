package com.seiama.sentinel.configuration;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.seiama.sentinel.common.jackson.ObjectIdSerializer;
import com.seiama.sentinel.common.jackson.SnowflakeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
}
