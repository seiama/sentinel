package com.seiama.sentinel.importer.source.manual;

import com.seiama.sentinel.importer.ImporterConstants;
import com.seiama.sentinel.importer.model.ImporterPunishment;
import com.seiama.sentinel.importer.model.ImporterPunishmentSource;
import java.time.Instant;
import org.jetbrains.annotations.Nullable;

public final class Manual implements ImporterPunishmentSource {
  public @Nullable Long guild;
  public ImporterPunishment.@Nullable Type type;
  public @Nullable Instant date;
  public @Nullable Long punisher_id;
  public @Nullable String punisher_username;
  public @Nullable String punisher_discriminator;
  public @Nullable Long punished_id;
  public @Nullable String punished_username;
  public @Nullable String punished_discriminator;
  public @Nullable String reason;
  public @Nullable Boolean automatic;
  public @Nullable Boolean expunged;
  public @Nullable Boolean stale;
  public @Nullable Boolean stale_automatic;
  public @Nullable Instant stale_at;
  public @Nullable Long stale_by_id;
  public @Nullable String stale_by_username;
  public @Nullable String stale_by_discriminator;
  public @Nullable String stale_reason;
  public @Nullable String import_by;
  public @Nullable String import_id;
  public @Nullable Instant import_at;

  @Override
  public ImporterPunishment asPunishment() {
    final ImporterPunishment punishment = new ImporterPunishment(this);
    punishment.values.guild = this.guild;
    punishment.values.type = this.type;
    punishment.values.date = this.date;
    punishment.values.punisher_id = this.punisher_id;
    punishment.values.punisher_username = this.punisher_username;
    punishment.values.punisher_discriminator = this.punisher_discriminator;
    punishment.values.punished_id = this.punished_id;
    punishment.values.punished_username = this.punished_username;
    punishment.values.punished_discriminator = this.punished_discriminator;
    punishment.values.reason = this.reason;
    punishment.values.automatic = this.automatic;
    punishment.values.expunged = this.expunged;
    punishment.values.stale = this.stale;
    punishment.values.stale_automatic = this.stale_automatic;
    punishment.values.stale_at = this.stale_at;
    punishment.values.stale_by_id = this.stale_by_id;
    punishment.values.stale_by_username = this.stale_by_username;
    punishment.values.stale_by_discriminator = this.stale_by_discriminator;
    punishment.values.stale_reason = this.stale_reason;
    punishment.values.import_by = this.import_by != null ? this.import_by : ImporterConstants.IMPORTER_MANUAL;
    punishment.values.import_id = this.import_id;
    punishment.values.import_at = this.import_at != null ? this.import_at : Instant.now();
    return punishment;
  }
}
