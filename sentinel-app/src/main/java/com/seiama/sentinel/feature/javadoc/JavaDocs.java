package com.seiama.sentinel.feature.javadoc;

import com.seiama.sentinel.common.model.GuildRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@NullMarked
public class JavaDocs {

  private final GuildRepository guilds;

  @Autowired
  private JavaDocs(final GuildRepository guilds) {
    this.guilds = guilds;
  }

  // TODO: Save here the javadocs links and add methods to parse that links into more detailed things

}
