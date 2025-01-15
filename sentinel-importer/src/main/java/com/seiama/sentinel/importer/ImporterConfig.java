package com.seiama.sentinel.importer;

import com.seiama.sentinel.importer.model.ImporterPunishment;
import com.seiama.sentinel.importer.model.ImporterPunishmentSource;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record ImporterConfig(
  long guild,
  List<UserIdHint> userIdHints,
  List<Merge> merges,
  List<SingleModifier> singleModifiers,
  List<ReasonModifier> reasonModifiers,
  LongSet currentlyEnforcedMutes
) {
  public record UserIdHint(
    String username,
    String discriminator,
    long id
  ) {
  }

  public record Merge(
    Class<? extends ImporterPunishmentSource> sourceType, Object sourceId,
    Class<? extends ImporterPunishmentSource> targetType, Object targetId,
    boolean eraseTargetId
  ) {
  }

  public record SingleModifier(
    Class<? extends ImporterPunishmentSource> type,
    Object id,
    Consumer<ImporterPunishment> modifier
  ) {
    public static Consumer<ImporterPunishment> DO_NOT_EXPORT = punishment -> punishment.meta.doNotExport = true;
  }

  public record ReasonModifier(
    @Nullable Class<? extends ImporterPunishmentSource> type,
    Predicate<@Nullable String> when,
    @Nullable String then
  ) {
  }
}
