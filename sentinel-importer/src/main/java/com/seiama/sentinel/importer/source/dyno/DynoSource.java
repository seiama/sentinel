package com.seiama.sentinel.importer.source.dyno;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.seiama.sentinel.importer.jackson.Json;
import com.seiama.sentinel.importer.source.Source;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class DynoSource implements Source<Dyno> {
  @Override
  public Stream<Dyno> parse(final Path path) throws IOException {
    return Lists.reverse(Json.read(path, "logs", new TypeReference<List<Dyno>>() {})).stream();
  }
}
