package com.seiama.sentinel.feature.javadoc;

public enum JavadocElementType {
  PACKAGE("Package"),
  CONSTRUCTOR("Constructor"),
  METHOD("Method"),
  ENUM_ELEMENT("Enum Element"),
  FIELD("Field"),
  CLASS("Class"),
  INTERFACE("Interface"),
  ENUM_CLASS("Enum Class"),
  RECORD_CLASS("Record Class"),
  ANNOTATION_INTERFACE("Annotation Interface"),
  EXCEPTION_CLASS("Exception Class"),
  UNKNOWN("Unknown");

  public final String displayName;

  JavadocElementType(String displayName) {
    this.displayName = displayName;
  }
}
