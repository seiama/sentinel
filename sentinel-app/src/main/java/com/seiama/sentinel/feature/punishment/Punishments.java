package com.seiama.sentinel.feature.punishment;

import com.seiama.sentinel.common.model.GuildRepository;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import java.util.Collections;
import java.util.function.Function;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;

public interface Punishments {
  static <T> Function<? super T, ? extends Publisher<Boolean>> mayPunish(
    final GuildRepository guilds,
    final Guild guild,
    final @Nullable Member member
  ) {
    return __ -> {
      return guilds.findByGuild(guild.getId())
        .map(model -> member != null && !Collections.disjoint(
          member.getRoleIds(),
          model.features().punishments().permissions().punish()
        ));
    };
  }
}
