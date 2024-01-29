package com.seiama.sentinel.configuration;

import io.r2dbc.spi.ConnectionFactory;
import org.jspecify.annotations.NullMarked;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@Configuration
@NullMarked
public class R2dbcConfiguration {
  @Bean
  ConnectionFactoryInitializer connectionFactoryInitializer(final ConnectionFactory connectionFactory) {
    final ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
    initializer.setConnectionFactory(connectionFactory);
    initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ClassPathResource("schema.sql")));
    return initializer;
  }
}
