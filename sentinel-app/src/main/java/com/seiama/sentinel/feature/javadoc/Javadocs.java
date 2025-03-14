package com.seiama.sentinel.feature.javadoc;

import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.common.model.JavadocRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@NullMarked
public class Javadocs {

  private final GuildRepository guilds;
  private final JavadocRepository javaDocs;

  @Autowired
  private Javadocs(final GuildRepository guilds, final JavadocRepository javadocs) {
    this.guilds = guilds;
    this.javaDocs = javadocs;
  }

  // TODO: Save here the javadocs links and add methods to parse that links into more detailed things

}
