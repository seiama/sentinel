package com.seiama.sentinel.feature.javadoc;

public enum JavadocElementType {
  PACKAGE("Package"),
  CONSTRUCTOR("Constructor"),
  METHOD("Method"),
  ENUM_ELEMENT("Enum Element"),
  FIELD("Field"),
  CLASS("Class"),
  INTERFACE("Interface"),
  RECORD("Record"),
  UNKNOWN("Unknown");

  public final String displayName;

  JavadocElementType(String displayName) {
    this.displayName = displayName;
  }
}
