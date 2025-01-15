package com.seiama.sentinel.importer.source.dyno;

import com.seiama.sentinel.importer.ImporterConstants;
import com.seiama.sentinel.importer.model.ImporterPunishment;
import com.seiama.sentinel.importer.model.ImporterPunishmentSource;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class Dyno implements ImporterPunishmentSource {
  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy h:mm a", Locale.US).withZone(ZoneId.of("America/New_York"));

  public static final String TYPE_BAN = "Ban";
  public static final String TYPE_BAN_AUTO = "Ban [Auto]";
  public static final String TYPE_KICK = "Kick";
  public static final String TYPE_MUTE = "Mute";
  public static final String TYPE_MUTE_AUTO = "Mute [Auto]";
  public static final String TYPE_WARN = "Warn";
  public static final String TYPE_UNBAN = "Unban";
  public static final String TYPE_UNMUTE = "Unmute";

  public String _id;
  public int caseNum;
  public long server;
  public String type;
  public User user;
  public @Nullable User mod;
  public String reason;
  public long message;
  public int v;
  public String createdAt;
  public int __v;

  @Override
  @SuppressWarnings("ObjectToString")
  public ImporterPunishment asPunishment() {
    final ImporterPunishment punishment = new ImporterPunishment(this);
    punishment.values.guild = this.server;
    punishment.values.type = switch (this.type) {
      case TYPE_BAN, TYPE_BAN_AUTO -> ImporterPunishment.Type.BAN;
      case TYPE_KICK -> ImporterPunishment.Type.KICK;
      case TYPE_WARN -> ImporterPunishment.Type.WARN;
      case TYPE_MUTE, TYPE_MUTE_AUTO -> ImporterPunishment.Type.MUTE;
      case TYPE_UNBAN -> ImporterPunishment.Type.UNBAN;
      case TYPE_UNMUTE -> ImporterPunishment.Type.UNMUTE;
      default -> throw new IllegalArgumentException("Unknown punishment type: " + this.type);
    };
    punishment.values.date = DATE_TIME_FORMATTER.parse(this.createdAt, Instant::from);
    if (this.mod != null) {
      punishment.values.punisherId = this.mod.id;
      punishment.values.punisherUsername = this.mod.username;
      punishment.values.punisherDiscriminator = this.mod.discriminator;
    } else if (Boolean.TRUE.equals(this.wasAutomatic())) {
      punishment.values.punisherId = ImporterConstants.BOT_DYNO_ID_PREMIUM;
      punishment.values.punisherUsername = ImporterConstants.BOT_DYNO_USERNAME_PREMIUM;
      punishment.values.punisherDiscriminator = ImporterConstants.BOT_DYNO_DISCRIMINATOR_PREMIUM;
    } else {
      throw new IllegalStateException("no moderator for" + this);
    }
    punishment.values.punishedId = this.user.id;
    punishment.values.punishedUsername = this.user.username;
    punishment.values.punishedDiscriminator = this.user.discriminator;
    punishment.values.reason = this.reason;
    punishment.values.automatic = this.wasAutomatic();
    punishment.values.importBy = ImporterConstants.IMPORTER_DYNO;
    punishment.values.importId = String.valueOf(this.caseNum);
    punishment.values.importAt = Instant.now();
    return punishment;
  }

  @SuppressWarnings("UnnecessaryParentheses")
  private Boolean wasAutomatic() {
    if (this.type.equals(TYPE_BAN_AUTO) || this.type.equals(TYPE_MUTE_AUTO) || ("Auto".equals(this.reason) && (this.type.equals(TYPE_UNBAN) || this.type.equals(TYPE_UNMUTE)))) return true;
    return null;
  }

  public static class User {
    public long id;
    public String username;
    public String discriminator;
    public String avatarURL;
  }
}
