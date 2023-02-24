package com.seiama.sentinel.importer.source.warship;

import com.fasterxml.jackson.core.type.TypeReference;
import com.seiama.sentinel.importer.jackson.Json;
import com.seiama.sentinel.importer.source.Source;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public final class WarshipSource implements Source<Warship> {
  @Override
  public Stream<Warship> parse(final Path path) throws IOException {
    return Json.read(path, "infractions", new TypeReference<List<Warship>>() {})
      .stream()
      .sorted(Comparator.comparingInt(o -> o.id));
  }
}
