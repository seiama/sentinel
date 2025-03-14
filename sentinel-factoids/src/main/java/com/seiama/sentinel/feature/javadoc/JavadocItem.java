package com.seiama.sentinel.feature.javadoc;

public record JavadocItem(
  String url,
  String type,
  String packagePath,
  String name,
  String description,
  boolean deprecated,
  String deprecatedMessage
) {
}
