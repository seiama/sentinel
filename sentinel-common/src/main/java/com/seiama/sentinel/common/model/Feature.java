package com.seiama.sentinel.common.model;

import java.util.function.Predicate;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record Feature(
  Predicate<GuildModel.Complete> enabledForGuild
) {
  public static final Feature PUNISHMENTS = new Feature(model -> model.featureEnabled(GuildModel.Complete.Features::punishments));
  public static final Feature APPEALS = new Feature(model -> model.featureEnabled(features -> features.punishments().appeals()));
  public static final Feature MODMAIL = new Feature(model -> model.featureEnabled(GuildModel.Complete.Features::modmail));
  public static final Feature EXPLOITS = new Feature(model -> model.featureEnabled(GuildModel.Complete.Features::exploits));
  public static final Feature FACTOIDS = new Feature(model -> model.featureEnabled(GuildModel.Complete.Features::factoids));
  public static final Feature LOGGING = new Feature(model -> model.featureEnabled(GuildModel.Complete.Features::logging));

  public boolean enabledForGuild(final GuildModel.Complete model) {
    return this.enabledForGuild.test(model);
  }
}
