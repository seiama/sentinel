package com.seiama.sentinel.importer.source.beemo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.seiama.sentinel.importer.jackson.Json;
import com.seiama.sentinel.importer.source.Source;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public final class BeemoSource implements Source<Beemo> {
  @Override
  public Stream<Beemo> parse(final Path path) throws IOException {
    return Json.read(path, new TypeReference<List<Beemo>>() {})
      .stream();
  }
}
