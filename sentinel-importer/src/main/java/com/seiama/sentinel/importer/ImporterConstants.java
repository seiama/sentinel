package com.seiama.sentinel.importer;

import com.seiama.sentinel.importer.model.ImporterPunishment;
import java.util.Set;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ImporterConstants {
  Set<ImporterPunishment.Type> REVERSALS = Set.of(ImporterPunishment.Type.UNBAN, ImporterPunishment.Type.UNMUTE);

  long BOT_BEEMO_ID = 515067662028636170L;
  String BOT_BEEMO_USERNAME = "Beemo";
  String BOT_BEEMO_DISCRIMINATOR = "4570";
  String BOT_BEEMO_REASON_ANTIRAID = "ANTIBOT: Userbot raid detected";

  long BOT_CARL_ID = 235148962103951360L;
  String BOT_CARL_USERNAME = "Carl-bot";
  String BOT_CARL_DISCRIMINATOR = "1536";

  long BOT_DYNO_ID_FREE = 155149108183695360L;
  String BOT_DYNO_USERNAME_FREE = "Dyno";
  String BOT_DYNO_DISCRIMINATOR_FREE = "3861";

  long BOT_DYNO_ID_PREMIUM = 168274283414421504L;
  String BOT_DYNO_USERNAME_PREMIUM = "Dyno";
  String BOT_DYNO_DISCRIMINATOR_PREMIUM = "7532";

  long BOT_WARSHIP_ID = 418322559981846528L;
  String BOT_WARSHIP_USERNAME = "Warship";
  String BOT_WARSHIP_DISCRIMINATOR = "5966";

  String IMPORTER_DISCORD = "DISCORD";
  String IMPORTER_BEEMO = "BEEMO";
  String IMPORTER_CARL = "CARL";
  String IMPORTER_DYNO = "DYNO";
  String IMPORTER_MANUAL = "MANUAL";
  String IMPORTER_WARSHIP = "WARSHIP";
}
