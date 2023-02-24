package com.seiama.sentinel.util;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.discordjson.json.EmbedAuthorData;
import discord4j.discordjson.json.UserData;
import discord4j.discordjson.possible.Possible;
import discord4j.rest.util.Image;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class Discord {
  private Discord() {
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public static boolean isBot(final @NotNull Optional<User> user) {
    return user.isPresent() && user.get().isBot();
  }

  public static @NotNull Optional<EmbedCreateFields.Author> author(final @Nullable Guild guild) {
    if (guild == null) return Optional.empty();
    return Optional.of(EmbedCreateFields.Author.of(guild.getName(), null, guild.getIconUrl(Image.Format.JPEG).orElse(null)));
  }

  public static @NotNull Optional<EmbedCreateFields.Author> author(final @Nullable User user) {
    if (user == null) return Optional.empty();
    return Optional.of(EmbedCreateFields.Author.of(user.getTag(), null, user.getAvatarUrl()));
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public static @NotNull EmbedAuthorData author(final @NotNull UserData data, final @NotNull Optional<User> user) {
    return EmbedAuthorData.builder()
      .name(data.username() + "#" + data.discriminator())
      .iconUrl(user.map(User::getAvatarUrl).map(Possible::of).orElse(Possible.absent()))
      .build();
  }
}
