package com.seiama.sentinel.importer.source.beemo;

import com.seiama.sentinel.importer.ImporterConstants;
import com.seiama.sentinel.importer.model.ImporterPunishment;
import com.seiama.sentinel.importer.model.ImporterPunishmentSource;
import java.time.Instant;

public final class Beemo implements ImporterPunishmentSource {
  public long guild;
  public Instant date;
  public User user;

  @Override
  public ImporterPunishment asPunishment() {
    final ImporterPunishment punishment = new ImporterPunishment(this);
    punishment.values.guild = this.guild;
    punishment.values.type = ImporterPunishment.Type.BAN;
    punishment.values.date = this.date;
    punishment.values.punisher_id = ImporterConstants.BOT_BEEMO_ID;
    punishment.values.punisher_username = ImporterConstants.BOT_BEEMO_USERNAME;
    punishment.values.punisher_discriminator = ImporterConstants.BOT_BEEMO_DISCRIMINATOR;
    punishment.values.punished_id = this.user.id;
    punishment.values.punished_username = this.user.username;
    punishment.values.punished_discriminator = this.user.discriminator;
    punishment.values.automatic = true;
    punishment.values.import_by = ImporterConstants.IMPORTER_BEEMO;
    punishment.values.import_id = null; // not provided
    punishment.values.import_at = Instant.now();
    return punishment;
  }

  public static class User {
    public long id;
    public String username;
    public String discriminator;
  }
}
