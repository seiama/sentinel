package com.seiama.sentinel.common.converter;

import com.seiama.sentinel.common.model.Discriminator;
import org.jspecify.annotations.NullMarked;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@NullMarked
@WritingConverter
public class WritingDiscriminatorConverter implements Converter<Discriminator, String> {
  @Override
  public String convert(final Discriminator source) {
    return Discriminator.unbox(source);
  }
}
