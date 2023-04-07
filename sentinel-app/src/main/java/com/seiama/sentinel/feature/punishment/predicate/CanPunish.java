package com.seiama.sentinel.feature.punishment.predicate;

import com.seiama.sentinel.common.model.GuildRepository;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import java.util.Collections;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;

public final class CanPunish<T> implements Function<T, Publisher<Boolean>> {
  private final GuildRepository guilds;
  private final Guild guild;
  private final @Nullable Member member;

  public CanPunish(final GuildRepository guilds, final Guild guild, final @Nullable Member member) {
    this.guilds = guilds;
    this.guild = guild;
    this.member = member;
  }

  @Override
  public Publisher<Boolean> apply(final T t1) {
    return this.guilds.findByGuild(this.guild.getId())
      .map(model -> this.member != null && !Collections.disjoint(
        this.member.getRoleIds(),
        model.features().punishments().permissions().punish()
      ));
  }
}
