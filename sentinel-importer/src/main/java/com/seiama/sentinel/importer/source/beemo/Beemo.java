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
    punishment.values.punisherId = ImporterConstants.BOT_BEEMO_ID;
    punishment.values.punisherUsername = ImporterConstants.BOT_BEEMO_USERNAME;
    punishment.values.punisherDiscriminator = ImporterConstants.BOT_BEEMO_DISCRIMINATOR;
    punishment.values.punishedId = this.user.id;
    punishment.values.punishedUsername = this.user.username;
    punishment.values.punishedDiscriminator = this.user.discriminator;
    punishment.values.automatic = true;
    punishment.values.importBy = ImporterConstants.IMPORTER_BEEMO;
    punishment.values.importId = null; // not provided
    punishment.values.importAt = Instant.now();
    return punishment;
  }

  public static class User {
    public long id;
    public String username;
    public String discriminator;
  }
}
