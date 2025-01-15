package com.seiama.sentinel.importer.jackson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class Json {
  private static final ObjectMapper MAPPER = new ObjectMapper()
    .registerModule(new JavaTimeModule());

  private Json() {
  }

  public static <T> T read(final Path path, final Class<T> type) throws IOException {
    return MAPPER.readValue(Files.readString(path), type);
  }

  public static <T> T read(final Path path, final TypeReference<T> type) throws IOException {
    return MAPPER.readValue(Files.readString(path), type);
  }

  public static <T> T read(final Path path, final String key, final TypeReference<T> type) throws IOException {
    return MAPPER.convertValue(MAPPER.readValue(Files.readString(path), JsonNode.class).get(key), type);
  }

  public static ObjectMapper mapper() {
    return MAPPER;
  }

  public static ObjectWriter writer() {
    return MAPPER.writer(new PrettyPrinter());
  }
}
