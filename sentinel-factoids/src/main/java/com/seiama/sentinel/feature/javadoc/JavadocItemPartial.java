package com.seiama.sentinel.feature.javadoc;

public record JavadocItemPartial(
  String url,
  String type,
  String packagePath,
  String name
) {

  public String nameSuggest() {
    return "%s [%s] (%s)".formatted(this.name, this.type, this.packagePath);
  }

}
