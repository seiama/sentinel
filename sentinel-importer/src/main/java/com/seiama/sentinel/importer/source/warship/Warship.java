package com.seiama.sentinel.importer.source.warship;

import com.seiama.sentinel.importer.ImporterConstants;
import com.seiama.sentinel.importer.model.ImporterPunishment;
import com.seiama.sentinel.importer.model.ImporterPunishmentSource;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class Warship implements ImporterPunishmentSource {
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME.withLocale(Locale.US).withZone(ZoneOffset.UTC);

  public static final int TYPE_KICK = 1;
  public static final int TYPE_BAN = 4;
  public static final int TYPE_TEMPMUTE = 5;
  public static final int TYPE_UNBAN = 6;
  public static final int TYPE_WARN = 8;

  public boolean active;
  public User actor;
  public String created_at;
  public String expires_at;
  public Guild guild;
  public int id;
  public String reason;
  public Type type;
  public User user;

  @Override
  public ImporterPunishment asPunishment() {
    final ImporterPunishment punishment = new ImporterPunishment(this);
    punishment.values.guild = this.guild.id;
    punishment.values.type = switch (this.type.id) {
      case TYPE_KICK -> ImporterPunishment.Type.KICK;
      case TYPE_BAN -> ImporterPunishment.Type.BAN;
      case TYPE_TEMPMUTE -> ImporterPunishment.Type.MUTE;
      case TYPE_UNBAN -> ImporterPunishment.Type.UNBAN;
      case TYPE_WARN -> ImporterPunishment.Type.WARN;
      default -> throw new IllegalArgumentException("Unknown punishment type: " + this.type.id);
    };
    punishment.values.date = DATE_TIME_FORMATTER.parse(this.created_at, Instant::from);
    punishment.values.punisherId = this.actor.id;
    punishment.values.punisherUsername = this.actor.username;
    punishment.values.punisherDiscriminator = String.format("%04d", this.actor.discriminator);
    punishment.values.punishedId = this.user.id;
    punishment.values.punishedUsername = this.user.username;
    punishment.values.punishedDiscriminator = String.format("%04d", this.user.discriminator);
    punishment.values.reason = this.reason;
    punishment.values.importBy = ImporterConstants.IMPORTER_WARSHIP;
    punishment.values.importId = String.valueOf(this.id);
    punishment.values.importAt = Instant.now();
    return punishment;
  }

  public static class User {
    public String avatar;
    public boolean bot;
    public int discriminator;
    public long id;
    public String public_flags;
    public String username;
  }

  public static class Guild {
    public boolean enabled;
    public long id;
    public String name;
    public String role;
  }

  public static class Type {
    public int id;
    public String name;
  }
}
