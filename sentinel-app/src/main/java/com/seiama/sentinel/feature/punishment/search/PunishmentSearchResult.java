package com.seiama.sentinel.feature.punishment.search;

import com.seiama.sentinel.common.model.PunishmentModel;
import discord4j.core.object.entity.User;
import java.util.List;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record PunishmentSearchResult(
  User user,
  List<PunishmentModel.Complete> punishments
) {
}
