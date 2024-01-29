package com.seiama.sentinel.importer;

import com.seiama.sentinel.importer.source.Source;
import com.seiama.sentinel.importer.source.carl.Carl;
import com.seiama.sentinel.importer.source.dyno.Dyno;
import com.seiama.sentinel.importer.source.manual.Manual;
import com.seiama.sentinel.importer.source.warship.Warship;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ImporterApplication {
  private static final Path SOURCES_2022_02_22 = Path.of("sources", "2023-02-22");
  private static final Path SOURCES_2022_02_28 = Path.of("sources", "2023-02-28");
  private static final Path SOURCES_2022_04_13 = Path.of("sources", "2023-04-13");

  private ImporterApplication() {
  }

  public static void main(final String[] args) throws IOException {
    final ImporterConfig config = new ImporterConfig(
      289587909051416579L,
      List.of(
        new ImporterConfig.UserIdHint(ImporterConstants.BOT_CARL_USERNAME, ImporterConstants.BOT_CARL_DISCRIMINATOR, ImporterConstants.BOT_CARL_ID),
        new ImporterConfig.UserIdHint("kashike", "8590", 105923848263753728L),
        new ImporterConfig.UserIdHint("Joshie", "0001", 194473148161327104L),
        new ImporterConfig.UserIdHint("DemonWav", "1989", 172586171057176576L)
      ),
      List.of(
        new ImporterConfig.Merge(Warship.class, 25325, Carl.class, 2268, false),
        new ImporterConfig.Merge(Warship.class, 26439, Carl.class, 2591, false),
        new ImporterConfig.Merge(Warship.class, 28413, Carl.class, 3642, false),
        new ImporterConfig.Merge(Warship.class, 28798, Manual.class, 28798, true),
        new ImporterConfig.Merge(Warship.class, 34167, Carl.class, 4842, false),
        new ImporterConfig.Merge(Warship.class, 40047, Carl.class, 5920, false),
        new ImporterConfig.Merge(Warship.class, 42949, Manual.class, 42949, true)
      ),
      List.of(
        new ImporterConfig.SingleModifier(Dyno.class, 792, ImporterConfig.SingleModifier.DO_NOT_EXPORT), // testing purposes
        new ImporterConfig.SingleModifier(Warship.class, 20859, ImporterConfig.SingleModifier.DO_NOT_EXPORT), // unban followed by another ban
        new ImporterConfig.SingleModifier(Warship.class, 28798, punishment -> {
          punishment.values.date = DateTimeFormatter.ISO_DATE_TIME.parse("2021-10-10T13:39:18.924017Z", Instant::from); // set to value from carl-bot #3650 to allow proper merging
        }),
        new ImporterConfig.SingleModifier(Warship.class, 39750, ImporterConfig.SingleModifier.DO_NOT_EXPORT), // how to handle this one??
        new ImporterConfig.SingleModifier(Warship.class, 48797, ImporterConfig.SingleModifier.DO_NOT_EXPORT) // how to handle this one??
      ),
      List.of(),
      LongSet.of()
    );

    final Importer importer = new Importer();

    importer.addPunishments(Source.BEEMO.parse(SOURCES_2022_02_28.resolve("beemo.json")));
    importer.addPunishments(Source.CARL.parse(SOURCES_2022_02_22.resolve("carl-bot.json")));
    importer.addPunishments(Source.DISCORD.parse(SOURCES_2022_04_13.resolve("discord.json")));
    importer.addPunishments(Source.DYNO.parse(SOURCES_2022_02_22.resolve("dyno.json")));
    importer.addPunishments(Source.MANUAL.parse(SOURCES_2022_02_22.resolve("manual.json")));
    importer.addPunishments(Source.WARSHIP.parse(SOURCES_2022_04_13.resolve("warship.json")));

    importer.process(config);

    importer.export(Path.of("exports", "punishments.json"));

    final ImporterSummary summary = new ImporterSummary();
    summary.print(importer);
  }
}
