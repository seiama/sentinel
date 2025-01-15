package com.seiama.sentinel.importer.source.discord;

import com.google.common.base.MoreObjects;
import com.seiama.sentinel.importer.ImporterConstants;
import com.seiama.sentinel.importer.model.ImporterPunishment;
import com.seiama.sentinel.importer.model.ImporterPunishmentSource;
import java.time.Instant;
import org.jetbrains.annotations.Nullable;

public final class Discord implements ImporterPunishmentSource {
  public User user;
  public @Nullable String reason;

  @Override
  public ImporterPunishment asPunishment() {
    final ImporterPunishment punishment = new ImporterPunishment(this);
    punishment.values.guild = null; // not provided
    punishment.values.type = ImporterPunishment.Type.BAN;
    punishment.values.date = null; // not provided
    punishment.values.punisher_id = null; // not provided
    punishment.values.punisher_username = null; // not provided
    punishment.values.punisher_discriminator = null; // not provided
    punishment.values.punished_id = this.user.id;
    punishment.values.punished_username = this.user.username;
    punishment.values.punished_discriminator = this.user.discriminator;
    punishment.values.import_by = ImporterConstants.IMPORTER_DISCORD;
    punishment.values.import_id = null; // not provided
    punishment.values.import_at = Instant.now();
    return punishment;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("user", this.user)
      .add("reason", this.reason)
      .toString();
  }

  public static class User {
    public long id;
    public String username;
    public String display_name;
    public String avatar;
    public String avatar_decoration;
    public String discriminator;
    public int public_flags;

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
        .add("id", this.id)
        .add("username", this.username)
        .add("display_name", this.display_name)
        .add("avatar", this.avatar)
        .add("avatar_decoration", this.avatar_decoration)
        .add("discriminator", this.discriminator)
        .add("public_flags", this.public_flags)
        .toString();
    }
  }
}
