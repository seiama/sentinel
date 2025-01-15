package com.seiama.sentinel.importer;

import com.seiama.sentinel.importer.model.ImporterPunishment;
import com.seiama.sentinel.importer.model.ImporterPunishmentSource;
import com.seiama.sentinel.importer.source.beemo.Beemo;
import com.seiama.sentinel.importer.source.carl.Carl;
import com.seiama.sentinel.importer.source.discord.Discord;
import com.seiama.sentinel.importer.source.dyno.Dyno;
import com.seiama.sentinel.importer.source.manual.Manual;
import com.seiama.sentinel.importer.source.warship.Warship;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

final class ImporterSummary {
  void print(final Importer importer) {
    final List<ImporterPunishment> punishments = importer.punishments.all();
    final List<ResultType> types = List.of(
      new ResultType(
        "Total",
        punishments.size(),
        source -> punishments.stream().filter(punishment -> source.isInstance(punishment.source)).count(),
        null
      ),
      new ResultType(
        "Merged",
        punishments.stream().filter(punishment -> punishment.meta.mergedIntoOther).count(),
        source -> punishments.stream().filter(punishment -> source.isInstance(punishment.source) && punishment.meta.mergedIntoOther).count(),
        null
      ),
      new ResultType(
        "Filtered",
        punishments.stream().filter(punishment -> punishment.meta.doNotExport).count(),
        source -> punishments.stream().filter(punishment -> source.isInstance(punishment.source) && punishment.meta.doNotExport).count(),
        null
      ),
      new ResultType(
        "Marked Stale",
        punishments.stream().filter(punishment -> punishment.meta.markedStale).count(),
        source -> punishments.stream().filter(punishment -> source.isInstance(punishment.source) && punishment.meta.markedStale).count(),
        null
      ),
      new ResultType(
        "Marked Automatic",
        punishments.stream().filter(punishment -> punishment.meta.markedAutomatic).count(),
        source -> punishments.stream().filter(punishment -> source.isInstance(punishment.source) && punishment.meta.markedAutomatic).count(),
        null
      ),
      new ResultType(
        "Importable (Total)",
        punishments.stream().filter(ImporterPunishment::exportable).count(),
        source -> punishments.stream().filter(punishment -> source.isInstance(punishment.source) && punishment.exportable()).count(),
        null
      ),
      new ResultType(
        "Importable (Active)",
        punishments.stream().filter(punishment -> punishment.exportable() && punishment.meta.currentlyEnforced).count(),
        source -> punishments.stream().filter(punishment -> source.isInstance(punishment.source) && punishment.exportable() && punishment.meta.currentlyEnforced).count(),
        null
      )
    );
    System.out.println("Punishments:");
    System.out.println("-".repeat(118));
    for (final ResultType type : types) {
      System.out.printf(
        "  %-22s: %-4d (Beemo: %-4d | Carl: %-4d | Discord: %-4d | Dyno: %-4d | Manual: %-4d | Warship: %-4d)%s%n",
        type.name(),
        type.total(),
        type.source().applyAsLong(Beemo.class),
        type.source().applyAsLong(Carl.class),
        type.source().applyAsLong(Discord.class),
        type.source().applyAsLong(Dyno.class),
        type.source().applyAsLong(Manual.class),
        type.source().applyAsLong(Warship.class),
        Optional.ofNullable(type.extra()).orElse(() -> "").get()
      );
    }
    System.out.println("-----");
    final List<String> out = new ArrayList<>();
    for (final ImporterPunishment punishment : punishments) {
      if (punishment.exportable()) {
        out.add(String.format(
          "%-6s %-20d %-50s: %s",
          punishment.values.type,
          punishment.values.punished_id,
          punishment.values.punished_username + "#" + punishment.values.punished_discriminator,
          punishment.values.reason != null ? punishment.values.reason.replace("\n", "\\n") : ""
        ));
      }
    }
    try {
      Files.writeString(Path.of("dump.txt"), out.stream().collect(Collectors.joining("\n")));
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  record ResultType(
    String name,
    long total,
    ToLongFunction<Class<? extends ImporterPunishmentSource>> source,
    Supplier<String> extra
  ) {
  }
}
