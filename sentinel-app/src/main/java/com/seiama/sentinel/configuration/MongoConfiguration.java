package com.seiama.sentinel.configuration;

import com.mongodb.MongoClientSettings;
import com.seiama.sentinel.common.bson.DiscriminatorCodec;
import com.seiama.sentinel.common.bson.SnowflakeCodec;
import com.seiama.sentinel.common.converter.ReadingDiscriminatorConverter;
import com.seiama.sentinel.common.converter.SnowflakeToStringConverter;
import com.seiama.sentinel.common.converter.WritingDiscriminatorConverter;
import java.util.List;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.jspecify.annotations.NullMarked;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
@NullMarked
public class MongoConfiguration {
  @Bean
  MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer() {
    return builder -> {
      builder.codecRegistry(CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromProviders(
          CodecRegistries.fromCodecs(
            new DiscriminatorCodec(),
            new SnowflakeCodec()
          ),
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
      new ReadingDiscriminatorConverter(),
      new WritingDiscriminatorConverter(),
      new SnowflakeToStringConverter()
    ));
  }
}
