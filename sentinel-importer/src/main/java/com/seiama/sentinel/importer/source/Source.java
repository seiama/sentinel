package com.seiama.sentinel.importer.source;

import com.seiama.sentinel.importer.source.beemo.BeemoSource;
import com.seiama.sentinel.importer.source.carl.CarlSource;
import com.seiama.sentinel.importer.source.discord.DiscordSource;
import com.seiama.sentinel.importer.source.dyno.DynoSource;
import com.seiama.sentinel.importer.source.manual.ManualSource;
import com.seiama.sentinel.importer.source.warship.WarshipSource;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Source<T> {
  BeemoSource BEEMO = new BeemoSource();
  CarlSource CARL = new CarlSource();
  DiscordSource DISCORD = new DiscordSource();
  DynoSource DYNO = new DynoSource();
  ManualSource MANUAL = new ManualSource();
  WarshipSource WARSHIP = new WarshipSource();

  Stream<T> parse(final Path path) throws IOException;
}
