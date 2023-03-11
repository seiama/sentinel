package com.seiama.sentinel.importer.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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
    this.values.staleAt = that.values.date;
    this.values.staleById = that.values.punisherId;
    this.values.staleByUsername = that.values.punisherUsername;
    this.values.staleByDiscriminator = that.values.punisherDiscriminator;
    this.values.staleReason = that.values.reason;
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
  @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
  @JsonPropertyOrder({
    "guild", "type", "date",
    "punisherId", "punisherUsername", "punisherDiscriminator",
    "punishedId", "punishedUsername", "punishedDiscriminator",
    "reason",
    "automatic",
    "expunged",
    "stale", "staleAutomatic", "staleAt", "staleById", "staleByUsername", "staleByDiscriminator", "staleReason",
    "importBy", "importId", "importAt"
  })
  public static final class Values {
    public @Nullable Long guild;
    public @Nullable Type type;
    @JsonSerialize(using = InstantExtendedJsonSerializer.class)
    public @Nullable Instant date;
    public @Nullable Long punisherId;
    public @Nullable String punisherUsername;
    public @Nullable String punisherDiscriminator;
    public @Nullable Long punishedId;
    public @Nullable String punishedUsername;
    public @Nullable String punishedDiscriminator;
    public @Nullable String reason;
    public @Nullable Boolean automatic;
    public @Nullable Boolean expunged;
    public @Nullable Boolean stale;
    public @Nullable Boolean staleAutomatic;
    @JsonSerialize(using = InstantExtendedJsonSerializer.class)
    public @Nullable Instant staleAt;
    public @Nullable Long staleById;
    public @Nullable String staleByUsername;
    public @Nullable String staleByDiscriminator;
    public @Nullable String staleReason;
    public @Nullable String importBy;
    public @Nullable String importId;
    @JsonSerialize(using = InstantExtendedJsonSerializer.class)
    public @Nullable Instant importAt;

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
        .add("guild", this.guild)
        .add("type", this.type)
        .add("date", this.date)
        .add("punisherId", this.punisherId)
        .add("punisherUsername", this.punisherUsername)
        .add("punisherDiscriminator", this.punisherDiscriminator)
        .add("punishedId", this.punishedId)
        .add("punishedUsername", this.punishedUsername)
        .add("punishedDiscriminator", this.punishedDiscriminator)
        .add("reason", this.reason)
        .add("automatic", this.automatic)
        .add("expunged", this.expunged)
        .add("stale", this.stale)
        .add("staleAutomatic", this.staleAutomatic)
        .add("staleAt", this.staleAt)
        .add("staleById", this.staleById)
        .add("staleByUsername", this.staleByUsername)
        .add("staleByDiscriminator", this.staleByDiscriminator)
        .add("staleReason", this.staleReason)
        .add("importby", this.importBy)
        .add("importId", this.importId)
        .add("importAt", this.importAt)
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
