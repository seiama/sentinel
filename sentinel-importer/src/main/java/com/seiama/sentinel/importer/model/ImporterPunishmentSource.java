package com.seiama.sentinel.importer.model;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ImporterPunishmentSource {
  ImporterPunishment asPunishment();
}
