package com.seiama.sentinel.feature.javadoc;

public enum JavadocElementType {
  UNKNOW("unknown"),
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

  public static JavadocElementType fromString(String name) {
    for (JavadocElementType type : values()) {
      if (type.name.equalsIgnoreCase(name)) {
        return type;
      }
    }
    return UNKNOW;
  }

}
