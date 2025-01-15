package com.seiama.sentinel.importer.jackson;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.core.util.Separators;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class PrettyPrinter extends DefaultPrettyPrinter {
  public PrettyPrinter() {
    this._arrayIndenter = DefaultIndenter.SYSTEM_LINEFEED_INSTANCE;
  }

  public PrettyPrinter(final PrettyPrinter base) {
    super(base);
  }

  @Override
  public PrettyPrinter withSeparators(final Separators separators) {
    this._separators = separators;
    this._objectFieldValueSeparatorWithSpaces = separators.getObjectFieldValueSeparator() + " ";
    return this;
  }

  @Override
  public PrettyPrinter createInstance() {
    return new PrettyPrinter(this);
  }
}
