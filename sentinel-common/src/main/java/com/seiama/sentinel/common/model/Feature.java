package com.seiama.sentinel.common.model;

import java.util.function.Predicate;

public record Feature(
  Predicate<GuildModel.Complete> enabledForGuild
) {
  public static final Feature PUNISHMENTS = new Feature(model -> model.features().punishments().enabled());
  public static final Feature PUNISHMENTS_APPEALS = new Feature(model -> model.features().punishments().appeals().enabled());

  public boolean enabledForGuild(final GuildModel.Complete model) {
    return this.enabledForGuild.test(model);
  }
}
