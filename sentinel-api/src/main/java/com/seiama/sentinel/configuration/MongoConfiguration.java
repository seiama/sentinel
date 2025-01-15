package com.seiama.sentinel.configuration;

import com.mongodb.MongoClientSettings;
import com.seiama.sentinel.common.bson.SnowflakeCodec;
import com.seiama.sentinel.common.converter.SnowflakeFromLongConverter;
import java.util.List;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
public class MongoConfiguration {
  @Bean
  MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer() {
    return builder -> {
      builder.codecRegistry(CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromProviders(
          CodecRegistries.fromCodecs(new SnowflakeCodec()),
          PojoCodecProvider.builder()
            .automatic(true)
            .build()
        )
      ));
    };
  }

  @Bean
  MongoCustomConversions customConversions() {
    return new MongoCustomConversions(List.of(
      new SnowflakeFromLongConverter()
    ));
  }
}
