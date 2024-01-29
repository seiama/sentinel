package com.seiama.sentinel.importer;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.seiama.sentinel.importer.jackson.Json;
import com.seiama.sentinel.importer.model.ImporterPunishment;
import com.seiama.sentinel.importer.model.ImporterPunishmentSource;
import com.seiama.sentinel.importer.model.ImporterPunishments;
import com.seiama.sentinel.importer.source.beemo.Beemo;
import com.seiama.sentinel.importer.source.carl.Carl;
import com.seiama.sentinel.importer.source.discord.Discord;
import com.seiama.sentinel.importer.source.dyno.Dyno;
import com.seiama.sentinel.importer.source.manual.Manual;
import com.seiama.sentinel.importer.source.warship.Warship;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class Importer {
  private static final List<ImporterConfig.ReasonModifier> DEFAULT_REASON_MODIFIERS = List.of(
    new ImporterConfig.ReasonModifier(Beemo.class, Objects::isNull, ImporterConstants.BOT_BEEMO_REASON_ANTIRAID),
    new ImporterConfig.ReasonModifier(Carl.class, ImporterUtil.matchStartEnd("No reason given, use `!reason ", " <text>` to add one"), null),
    new ImporterConfig.ReasonModifier(null, "None"::equals, null),
    new ImporterConfig.ReasonModifier(null, "No reason given."::equals, null)
  );

  final ImporterPunishments punishments = new ImporterPunishments();

  public void addPunishments(final Stream<? extends ImporterPunishmentSource> sources) {
    this.punishments.add(sources.map(ImporterPunishmentSource::asPunishment).toList());
  }

  // Order is very important here.
  public void process(final ImporterConfig config) {
    this.resolveIds(config.userIdHints());
    this.resolveReason(DEFAULT_REASON_MODIFIERS);
    this.resolveReason(config.reasonModifiers());
    this.resolveAutomatic();
    this.mergeAutomatically();
    this.mergeDuplicates();
    this.mergeManually(config.merges());
    this.mutate(config.singleModifiers());
    this.resolveActive(config.currentlyEnforcedMutes());
    this.verifyComplete(config.guild());
    this.resolveAutomaticallyStaleDueToReplacement();
    this.verifyOnlyOneActivePunishment();
  }

  private void resolveActive(final LongSet currentlyEnforcedMutes) {
    for (final ImporterPunishment punishment : this.punishments) {
      if (punishment.values.type == ImporterPunishment.Type.BAN) {
        if (punishment.values.punishedId != null) {
          for (final ImporterPunishment activeBan : this.punishments.all(Discord.class)) {
            if (ImporterUtil.equalsNotNull(punishment.values.punishedId, activeBan.values.punishedId)) {
              punishment.meta.currentlyEnforced = true;
              if (!Boolean.TRUE.equals(punishment.values.stale)) {
                activeBan.meta.mergedIntoOther = true;
              }
              break;
            }
          }
        }
      }
    }
    for (final ImporterPunishment punishment : this.punishments) {
      if (punishment.values.type == ImporterPunishment.Type.MUTE) {
        if (punishment.values.punishedId != null) {
          if (currentlyEnforcedMutes.contains(punishment.values.punishedId.longValue())) {
            punishment.meta.currentlyEnforced = true;
          }
        }
      }
    }
    for (final ImporterPunishment punishment : this.punishments) {
      if (punishment.values.type == ImporterPunishment.Type.BAN || punishment.values.type == ImporterPunishment.Type.MUTE) {
        if (!punishment.meta.currentlyEnforced) {
          punishment.values.stale = true;
          punishment.meta.markedStale = true;
        }
      }
    }
  }

  private void resolveAutomatic() {
    final List<Map.Entry<String, String>> matches = List.of(
      Map.entry("Automatic action carried out for posting links (", ")."),
      Map.entry("Automatic action carried out for spamming mentions (", ") mentions)."),
      Map.entry("Automatic action carried out for using a blacklisted word (", ").")
    );
    for (final ImporterPunishment punishment : this.punishments.all(Carl.class)) {
      final @Nullable String reason = punishment.values.reason;
      if (reason != null) {
        for (final Map.Entry<String, String> match : matches) {
          if (reason.startsWith(match.getKey()) && reason.endsWith(match.getValue())) {
            punishment.values.automatic = true;
            punishment.meta.markedAutomatic = true;
            break;
          }
        }
      }
    }
  }

  private void resolveIds(final List<ImporterConfig.UserIdHint> userIdHints) {
    for (final ImporterPunishment punishment : this.punishments) {
      for (final ImporterConfig.UserIdHint hint : userIdHints) {
        if (punishment.values.punisherId == null && Objects.equals(punishment.values.punisherUsername, hint.username()) && Objects.equals(punishment.values.punisherDiscriminator, hint.discriminator())) {
          punishment.values.punisherId = hint.id();
        }
      }
    }
  }

  private void resolveReason(final List<ImporterConfig.ReasonModifier> modifiers) {
    for (final ImporterConfig.ReasonModifier modifier : modifiers) {
      for (final ImporterPunishment punishment : this.punishments.all(modifier.type())) {
        if (modifier.when().test(punishment.values.reason)) {
          punishment.values.reason = modifier.then();
        }
      }
    }
  }

  private void resolveAutomaticallyStaleDueToReplacement() {
    final Long2ObjectMap<List<ImporterPunishment>> punishmentsByPunishedId = this.punishments.byPunishedId(Importer::isExportableActiveBan);
    for (final Long2ObjectMap.Entry<List<ImporterPunishment>> entry : punishmentsByPunishedId.long2ObjectEntrySet()) {
      entry.getValue().sort(Comparator.comparing(o -> o.values.date));
      if (entry.getValue().size() > 1) {
        for (final Iterator<ImporterPunishment> it = entry.getValue().iterator(); it.hasNext();) {
          final ImporterPunishment punishment = it.next();
          if (it.hasNext()) {
            punishment.values.stale = true;
            punishment.values.staleAutomatic = true;
            punishment.meta.markedStale = true;
          }
        }
      }
    }
  }

  private void mergeAutomatically() {
    final Map<Class<? extends ImporterPunishmentSource>, Predicate<ImporterPunishment>> types = Map.ofEntries(
      Map.entry(Carl.class, punishment -> punishment.source instanceof Carl),
      Map.entry(Dyno.class, punishment -> punishment.source instanceof Carl || punishment.source instanceof Discord || punishment.source instanceof Dyno || punishment.source instanceof Manual),
      Map.entry(Warship.class, punishment -> true)
    );
    types.forEach((source, target) -> this.merge(punishment -> source.isInstance(punishment.source), target));
  }

  private void mergeDuplicates() {
    for (final ImporterPunishment sourcePunishment : this.punishments) {
      for (final ImporterPunishment targetPunishment : this.punishments) {
        if (canMergeDuplicate(sourcePunishment, targetPunishment)) {
          if (Boolean.TRUE.equals(targetPunishment.values.stale)) {
            sourcePunishment.stale(targetPunishment);
          }
          targetPunishment.meta.mergedIntoOther = true;
          break;
        }
      }
    }
  }

  @SuppressWarnings("UnnecessaryParentheses")
  private void mergeManually(final List<ImporterConfig.Merge> merges) {
    dancing:
    for (final ImporterConfig.Merge merge : merges) {
      for (final ImporterPunishment sourcePunishment : this.punishments) {
        for (final ImporterPunishment targetPunishment : this.punishments) {
          if ((merge.sourceType().isInstance(sourcePunishment.source) && String.valueOf(merge.sourceId()).equals(sourcePunishment.values.importId)) &&
              (merge.targetType().isInstance(targetPunishment.source) && String.valueOf(merge.targetId()).equals(targetPunishment.values.importId))) {
            this.merge(sourcePunishment, targetPunishment, true);
            if (merge.eraseTargetId()) {
              targetPunishment.values.importId = null;
            }
            continue dancing;
          }
        }
      }
    }
  }

  private void merge(final Predicate<ImporterPunishment> isSource, final Predicate<ImporterPunishment> isTarget) {
    for (final ImporterPunishment sourcePunishment : this.punishments) {
      if (isSource.test(sourcePunishment) && ImporterConstants.REVERSALS.contains(sourcePunishment.values.type)) {
        for (final ImporterPunishment targetPunishment : this.punishments) {
          if (isTarget.test(targetPunishment)) {
            this.merge(sourcePunishment, targetPunishment, false);
          }
        }
      }
    }
  }

  private void merge(final ImporterPunishment sourcePunishment, final ImporterPunishment targetPunishment, final boolean force) {
    if ((force || canMerge(sourcePunishment, targetPunishment)) && !sourcePunishment.meta.mergedIntoOther) {
      targetPunishment.stale(sourcePunishment);
      sourcePunishment.meta.mergedIntoOther = true;
    }
  }

  @SuppressWarnings("UnnecessaryParentheses")
  private static boolean canMerge(final ImporterPunishment sourcePunishment, final ImporterPunishment targetPunishment) {
    return ImporterUtil.matches(sourcePunishment, targetPunishment, List.of(
      (source, target) -> !source.meta.mergedIntoOther && !target.meta.mergedIntoOther,
      (source, target) -> (source.values.type == ImporterPunishment.Type.UNBAN && target.values.type == ImporterPunishment.Type.BAN) || (source.values.type == ImporterPunishment.Type.UNMUTE && target.values.type == ImporterPunishment.Type.MUTE),
      (source, target) -> source.values.date != null && target.values.date != null && (source.values.date.isAfter(target.values.date) || source.values.date.equals(target.values.date)),
      (source, target) -> Objects.equals(source.values.punishedId, target.values.punishedId)
    ));
  }

  private static boolean canMergeDuplicate(final ImporterPunishment sourcePunishment, final ImporterPunishment targetPunishment) {
    return ImporterUtil.matches(sourcePunishment, targetPunishment, List.of(
      (source, target) -> source != target,
      (source, target) -> source.source.getClass() != target.source.getClass(),
      (source, target) -> !source.meta.mergedIntoOther && !target.meta.mergedIntoOther,
      (source, target) -> source.values.type == target.values.type,
      (source, target) -> source.values.date != null && target.values.date != null && source.values.date.equals(target.values.date),
      (source, target) -> Objects.equals(source.values.punisherId, target.values.punisherId),
      (source, target) -> Objects.equals(source.values.punishedId, target.values.punishedId),
      (source, target) -> Objects.equals(source.values.reason, target.values.reason),
      (source, target) -> !ImporterUtil.equalsNotNull(source.values.importId, target.values.importId)
    ));
  }

  private void mutate(final List<ImporterConfig.SingleModifier> modifiers) {
    for (final ImporterConfig.SingleModifier modifier : modifiers) {
      for (final ImporterPunishment punishment : this.punishments) {
        if (modifier.type().isInstance(punishment.source) && Objects.equals(punishment.values.importId, String.valueOf(modifier.id()))) {
          modifier.modifier().accept(punishment);
        }
      }
    }
  }

  private void verifyComplete(final long guild) {
    for (final ImporterPunishment punishment : this.punishments) {
      if (punishment.exportable()) {
        final boolean skip = punishment.source instanceof Discord || punishment.source instanceof Manual;
        if (punishment.values.guild == null) punishment.values.guild = guild;
        if (punishment.values.type == null) System.out.println("Punishment is missing type: " + punishment);
        if (punishment.values.date == null) System.out.println("Punishment is missing date: " + punishment);
        if (!skip && punishment.values.punisherId == null) System.out.println("Punishment is missing punisher_id: " + punishment);
        if (!skip && punishment.values.punisherUsername == null) System.out.println("Punishment is missing punisher_username: " + punishment);
        if (!skip && punishment.values.punisherDiscriminator == null) System.out.println("Punishment is missing punisher_discriminator: " + punishment);
        if (punishment.values.punishedId == null) System.out.println("Punishment is missing punished_id: " + punishment);
        if (punishment.values.punishedUsername == null) System.out.println("Punishment is missing punished_username: " + punishment);
        if (punishment.values.punishedDiscriminator == null) System.out.println("Punishment is missing punished_discriminator: " + punishment);
      }
    }
  }

  private void verifyOnlyOneActivePunishment() {
    final Long2ObjectMap<List<ImporterPunishment>> punishmentsByPunishedId = this.punishments.byPunishedId(Importer::isExportableActiveBan);
    for (final Long2ObjectMap.Entry<List<ImporterPunishment>> entry : punishmentsByPunishedId.long2ObjectEntrySet()) {
      entry.getValue().sort(Comparator.comparing(o -> o.values.date));
      if (entry.getValue().size() > 1) {
        System.out.println("Multiple punishments active for " + entry.getLongKey());
      }
    }
  }

  private static boolean isExportableActiveBan(final ImporterPunishment punishment) {
    return punishment.exportable() &&
           punishment.values.type == ImporterPunishment.Type.BAN &&
           !Boolean.TRUE.equals(punishment.values.stale);
  }

  public void export(final Path path) throws IOException {
    final ArrayNode punishments = Json.mapper().createArrayNode();
    for (final ImporterPunishment punishment : this.punishments) {
      if (punishment.exportable()) {
        punishments.add(Json.mapper().valueToTree(punishment.values));
      }
    }
    Files.writeString(path, Json.writer().writeValueAsString(punishments));
  }
}
