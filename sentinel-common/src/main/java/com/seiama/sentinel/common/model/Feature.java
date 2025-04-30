package com.seiama.sentinel.common.model;

import java.util.function.Predicate;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record Feature(
  Predicate<GuildModel> enabledForGuild
) {
  public static final Feature PUNISHMENTS = new Feature(model -> model.featureEnabled(GuildModel.Features::punishments));
  public static final Feature APPEALS = new Feature(model -> model.featureEnabled(features -> features.punishments().appeals()));
  public static final Feature MODMAIL = new Feature(model -> model.featureEnabled(GuildModel.Features::modmail));
  public static final Feature FACTOIDS = new Feature(model -> model.featureEnabled(GuildModel.Features::factoids));
  public static final Feature LOGGING = new Feature(model -> model.featureEnabled(GuildModel.Features::logging));

  public boolean enabledForGuild(final GuildModel model) {
    return this.enabledForGuild.test(model);
  }
}
