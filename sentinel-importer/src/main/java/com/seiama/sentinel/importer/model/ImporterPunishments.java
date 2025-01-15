package com.seiama.sentinel.importer.model;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class ImporterPunishments implements Iterable<ImporterPunishment> {
  final List<ImporterPunishment> punishments = new ArrayList<>();

  public void add(final List<ImporterPunishment> punishments) {
    this.punishments.addAll(punishments);
  }

  @Override
  public Iterator<ImporterPunishment> iterator() {
    return this.punishments.iterator();
  }

  public List<ImporterPunishment> all() {
    return this.punishments;
  }

  public List<ImporterPunishment> all(final @Nullable Class<? extends ImporterPunishmentSource> source) {
    return this.punishments.stream()
      .filter(punishment -> source == null || source.isInstance(punishment.source))
      .toList();
  }

  public Long2ObjectMap<List<ImporterPunishment>> byPunishedId(final Predicate<ImporterPunishment> filter) {
    final Long2ObjectMap<List<ImporterPunishment>> punishmentsByPunishedId = new Long2ObjectOpenHashMap<>();
    for (final ImporterPunishment punishment : this.punishments) {
      if (filter.test(punishment)) {
        assert punishment.values.punishedId != null;
        @Nullable List<ImporterPunishment> punishments = punishmentsByPunishedId.get(punishment.values.punishedId.longValue());
        if (punishments == null) {
          punishments = new ArrayList<>();
          punishmentsByPunishedId.put(punishment.values.punishedId.longValue(), punishments);
        }
        punishments.add(punishment);
      }
    }
    return punishmentsByPunishedId;
  }
}
