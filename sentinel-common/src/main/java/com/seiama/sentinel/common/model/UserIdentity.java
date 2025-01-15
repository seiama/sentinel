package com.seiama.sentinel.common.model;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.User;
import discord4j.discordjson.json.UserData;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static java.util.Objects.requireNonNull;

@NullMarked
public record UserIdentity(
  Snowflake id,
  String username,
  @Deprecated
  @Nullable Discriminator discriminator
) {
  public UserIdentity {
    requireNonNull(id, "id");
    requireNonNull(username, "username");
  }

  public UserIdentity(final User user) {
    this(
      user.getId(),
      user.getUsername(),
      new Discriminator(user)
    );
  }

  public UserIdentity(final UserData user) {
    this(
      Snowflake.of(user.id()),
      user.username(),
      new Discriminator(user)
    );
  }
}
