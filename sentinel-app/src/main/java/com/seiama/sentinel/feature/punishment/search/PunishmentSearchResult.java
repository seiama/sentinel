package com.seiama.sentinel.feature.punishment.search;

import com.seiama.sentinel.common.model.PunishmentModel;
import discord4j.core.object.entity.User;
import java.util.List;

public record PunishmentSearchResult(
  User user,
  List<PunishmentModel.Complete> punishments
) {
}
