package com.seiama.sentinel.importer.source.carl;

import com.seiama.sentinel.importer.ImporterConstants;
import com.seiama.sentinel.importer.model.ImporterPunishment;
import com.seiama.sentinel.importer.model.ImporterPunishmentSource;
import java.time.Instant;
import org.jetbrains.annotations.Nullable;

public final class Carl implements ImporterPunishmentSource {
  public static final String TYPE_BAN = "ban";
  public static final String TYPE_MUTE = "mute";
  public static final String TYPE_UNBAN = "unban";
  public static final String TYPE_UNMUTE = "unmute";

  public int id;
  public @Nullable Long guild;
  public String type;
  public @Nullable String date;
  public @Nullable String punisher_id;
  public @Nullable String punisher_username;
  public @Nullable String punisher_discriminator;
  public @Nullable Long punished_id;
  public @Nullable String punished_username;
  public @Nullable String punished_discriminator;
  public @Nullable String reason;
  public @Nullable String length;

  @Override
  public ImporterPunishment asPunishment() {
    final ImporterPunishment punishment = new ImporterPunishment(this);
    punishment.values.type = switch (this.type) {
      case TYPE_BAN -> ImporterPunishment.Type.BAN;
      case TYPE_MUTE -> ImporterPunishment.Type.MUTE;
      case TYPE_UNBAN -> ImporterPunishment.Type.UNBAN;
      case TYPE_UNMUTE -> ImporterPunishment.Type.UNMUTE;
      default -> throw new IllegalArgumentException("Unknown punishment type: " + this.type);
    };
    punishment.values.date = this.date != null ? Instant.parse(this.date) : null;
    punishment.values.punisher_id = (this.punisher_id != null && !this.punisher_id.isEmpty()) ? Long.parseLong(this.punisher_id) : null;
    punishment.values.punisher_username = this.punisher_username;
    punishment.values.punisher_discriminator = this.punisher_discriminator;
    punishment.values.punished_id = this.punished_id;
    punishment.values.punished_username = this.punished_username;
    punishment.values.punished_discriminator = this.punished_discriminator;
    punishment.values.reason = this.reason;
    punishment.values.import_by = ImporterConstants.IMPORTER_CARL;
    punishment.values.import_id = String.valueOf(this.id);
    punishment.values.import_at = Instant.now();
    return punishment;
  }
}
