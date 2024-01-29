package com.seiama.sentinel.feature.punishment.predicate;

import com.seiama.sentinel.common.model.GuildModel;
import com.seiama.sentinel.common.model.GuildRepository;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import discord4j.rest.http.client.ClientException;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

@NullMarked
public final class CanPunish<T> implements Function<User, Publisher<Boolean>> {
  private final GuildRepository guilds;
  private final Guild guild;
  private final @Nullable Member member;

  public CanPunish(final GuildRepository guilds, final Guild guild, final @Nullable Member member) {
    this.guilds = guilds;
    this.guild = guild;
    this.member = member;
  }

  @Override
  public Publisher<Boolean> apply(final User punished) {
    return this.guilds.findByGuild(this.guild.getId())
      .zipWhen(model ->
        this.guild.getMemberById(punished.getId())
          .map(Optional::of)
          .onErrorResume(ClientException.class, e -> Mono.just(Optional.empty()))
      )
      .map(TupleUtils.function(this::apply));
  }

  private boolean apply(final GuildModel.Complete model, final Optional<Member> punished) {
    return this.member != null && !Collections.disjoint(
      this.member.getRoleIds(),
      model.features().punishments().permissions().punish()
    ) && punished.map(member -> Collections.disjoint(
      member.getRoleIds(),
      model.features().punishments().permissions().exempt()
    )).orElse(punished.isEmpty());
  }
}
