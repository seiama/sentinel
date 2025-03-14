package com.seiama.sentinel.feature.javadoc;

public enum JavadocElementType {
  CLASS("class"),
  INTERFACE("interface"),
  ANNOTATION("annotation"),
  ENUM("enum"),
  ;

  private final String name;

  JavadocElementType(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
