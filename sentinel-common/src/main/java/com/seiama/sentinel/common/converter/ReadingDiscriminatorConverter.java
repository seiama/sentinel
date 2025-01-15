package com.seiama.sentinel.common.converter;

import com.seiama.sentinel.common.model.Discriminator;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public class ReadingDiscriminatorConverter implements Converter<String, Discriminator> {
  @Override
  public Discriminator convert(final String source) {
    return new Discriminator(source);
  }
}
