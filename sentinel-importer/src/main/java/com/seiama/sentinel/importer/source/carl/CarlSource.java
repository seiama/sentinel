package com.seiama.sentinel.importer.source.carl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.seiama.sentinel.importer.jackson.Json;
import com.seiama.sentinel.importer.source.Source;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class CarlSource implements Source<Carl> {
  @Override
  public Stream<Carl> parse(final Path path) throws IOException {
    return Json.read(path, new TypeReference<List<Carl>>() {})
      .stream()
      .sorted(Comparator.comparingInt(o -> o.id));
  }
}
