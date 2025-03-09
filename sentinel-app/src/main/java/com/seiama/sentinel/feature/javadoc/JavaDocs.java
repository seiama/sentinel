package com.seiama.sentinel.feature.javadoc;

import com.seiama.sentinel.common.model.GuildRepository;
import com.seiama.sentinel.common.model.JavaDocRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@NullMarked
public class JavaDocs {

  private final GuildRepository guilds;
  private final JavaDocRepository javaDocs;

  @Autowired
  private JavaDocs(final GuildRepository guilds, final JavaDocRepository javaDocs) {
    this.guilds = guilds;
    this.javaDocs = javaDocs;
  }

  // TODO: Save here the javadocs links and add methods to parse that links into more detailed things

}
