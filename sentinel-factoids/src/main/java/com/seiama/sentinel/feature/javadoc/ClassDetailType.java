package com.seiama.sentinel.feature.javadoc;

public enum ClassDetailType {

  FIELD("field-detail", "field"),
  CONSTRUCTOR("constructor-detail", "constructor"),
  METHOD("method-detail", "method"),
  ANNOTATION_ELEMENT("annotation-interface-element-detail", "annotation"),
  ENUM_CONSTANTS("enum-constant-detail", "enum"),;

  public final String detailId;
  public final String displayName;

  ClassDetailType(final String detailId, final String displayName) {
    this.detailId = detailId;
    this.displayName = displayName;
  }

}
