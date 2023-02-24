package com.seiama.sentinel.importer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.MoreObjects;
import com.seiama.sentinel.common.jackson.InstantExtendedJsonSerializer;
import java.time.Instant;
import org.jetbrains.annotations.Nullable;

public final class ImporterPunishment {
  public final ImporterPunishmentSource source;
  public final Values values = new Values();
  public final Meta meta = new Meta();

  public ImporterPunishment(final ImporterPunishmentSource source) {
    this.source = source;
  }

  public void stale(final ImporterPunishment that) {
    this.values.stale = true;
    this.values.stale_at = that.values.date;
    this.values.stale_by_id = that.values.punisher_id;
    this.values.stale_by_username = that.values.punisher_username;
    this.values.stale_by_discriminator = that.values.punisher_discriminator;
    this.values.stale_reason = that.values.reason;
  }

  public boolean exportable() {
    return !this.meta.mergedIntoOther && !this.meta.doNotExport;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("source", this.source)
      .add("values", this.values)
      .toString();
  }

  @JsonInclude(JsonInclude.Include.NON_NULL)
  @JsonPropertyOrder({
    "guild", "type", "date",
    "punisher_id", "punisher_username", "punisher_discriminator",
    "punished_id", "punished_username", "punished_discriminator",
    "reason",
    "automatic",
    "expunged",
    "stale", "stale_automatic", "stale_at", "stale_by_id", "stale_by_username", "stale_by_discriminator", "stale_reason",
    "import_by", "import_id", "import_at"
  })
  public static final class Values {
    public @Nullable Long guild;
    public @Nullable Type type;
    @JsonSerialize(using = InstantExtendedJsonSerializer.class)
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
    @JsonSerialize(using = InstantExtendedJsonSerializer.class)
    public @Nullable Instant stale_at;
    public @Nullable Long stale_by_id;
    public @Nullable String stale_by_username;
    public @Nullable String stale_by_discriminator;
    public @Nullable String stale_reason;
    public @Nullable String import_by;
    public @Nullable String import_id;
    @JsonSerialize(using = InstantExtendedJsonSerializer.class)
    public @Nullable Instant import_at;

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
        .add("guild", this.guild)
        .add("type", this.type)
        .add("date", this.date)
        .add("punisher_id", this.punisher_id)
        .add("punisher_username", this.punisher_username)
        .add("punisher_discriminator", this.punisher_discriminator)
        .add("punished_id", this.punished_id)
        .add("punished_username", this.punished_username)
        .add("punished_discriminator", this.punished_discriminator)
        .add("reason", this.reason)
        .add("automatic", this.automatic)
        .add("expunged", this.expunged)
        .add("stale", this.stale)
        .add("stale_automatic", this.stale_automatic)
        .add("stale_at", this.stale_at)
        .add("stale_by_id", this.stale_by_id)
        .add("stale_by_username", this.stale_by_username)
        .add("stale_by_discriminator", this.stale_by_discriminator)
        .add("stale_reason", this.stale_reason)
        .add("import_by", this.import_by)
        .add("import_id", this.import_id)
        .add("import_at", this.import_at)
        .toString();
    }
  }

  @SuppressWarnings("SingleSpaceSeparator")
  public static final class Meta {
    public boolean currentlyEnforced; // If this punishment is being actively enforced
    public boolean doNotExport;       // Explicitly do not export
    public boolean markedAutomatic;   // If we manually marked this punishment as automatic
    public boolean markedStale;       // If we manually marked this punishment as stale
    public boolean mergedIntoOther;   // If this punishment has been merged into another
  }

  public enum Type {
    BAN,
    KICK,
    MUTE,
    WARN,
    UNBAN,
    UNMUTE;
  }
}
