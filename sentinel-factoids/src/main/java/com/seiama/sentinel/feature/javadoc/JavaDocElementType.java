package com.seiama.sentinel.feature.javadoc;

public enum JavaDocElementType {
  CLASS("class"),
  INTERFACE("interface"),
  ANNOTATION("annotation"),
  ENUM("enum"),
  ;

  private final String name;

  JavaDocElementType(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }
}
