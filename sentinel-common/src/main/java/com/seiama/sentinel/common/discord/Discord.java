package com.seiama.sentinel.common.discord;

import com.seiama.sentinel.common.model.UserIdentity;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateFields;
import discord4j.discordjson.json.UserData;
import discord4j.rest.util.Image;
import java.util.Optional;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public final class Discord {
  private Discord() {
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public static boolean isBot(final Optional<User> user) {
    return user.isPresent() && user.get().isBot();
  }

  public static Optional<EmbedCreateFields.Author> author(final @Nullable Guild guild) {
    if (guild == null) return Optional.empty();
    return Optional.of(EmbedCreateFields.Author.of(guild.getName(), null, guild.getIconUrl(Image.Format.JPEG).orElse(null)));
  }

  public static Optional<EmbedCreateFields.Author> author(final @Nullable User user) {
    if (user == null) return Optional.empty();
    return Optional.of(EmbedCreateFields.Author.of(user.getTag(), null, user.getAvatarUrl()));
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  public static EmbedCreateFields.Author author(final UserData data, final Optional<User> user) {
    return EmbedCreateFields.Author.of(
      UserDisplay.render(UserDisplay.Renderer.USERNAME, new UserIdentity(data)),
      null,
      user.map(User::getAvatarUrl).orElse(null)
    );
  }
}
