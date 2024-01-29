package com.seiama.sentinel.importer.source.manual;

import com.fasterxml.jackson.core.type.TypeReference;
import com.seiama.sentinel.importer.jackson.Json;
import com.seiama.sentinel.importer.source.Source;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ManualSource implements Source<Manual> {
  @Override
  public Stream<Manual> parse(final Path path) throws IOException {
    return Json.read(path, new TypeReference<List<Manual>>() {})
      .stream();
  }
}
