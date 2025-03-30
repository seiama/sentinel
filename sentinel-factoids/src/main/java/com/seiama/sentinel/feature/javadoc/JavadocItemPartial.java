package com.seiama.sentinel.feature.javadoc;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import net.maisikoleni.javadoc.entities.SearchableEntity;

public record JavadocItemPartial(
  String url,
  String name,
  String qualifiedName,
  JavadocComponentType type
) {

  public String displayName() {
    if (this.type.equals(JavadocComponentType.TYPE) || this.type.equals(JavadocComponentType.MEMBER)) {
      String[] parts = this.qualifiedName.split("\\.");
      int classNameIndex = -1;

      for (int i = 0; i < parts.length - 1; i++) {
        if (Character.isUpperCase(parts[i].charAt(0))) {
          classNameIndex = i;
          break;
        }
      }

      if (classNameIndex == -1) {
        return this.qualifiedName;
      }

      String className = String.join(".", Arrays.copyOfRange(parts, classNameIndex, parts.length - 1));
      String methodFieldName = parts[parts.length - 1];

      String packageName = String.join(".", Arrays.copyOfRange(parts, 0, classNameIndex));

      return className + (this.type == JavadocComponentType.TYPE ? "." : "#") + methodFieldName + " [" + packageName + "]";
    }
    return this.qualifiedName;
  }

  public String displayTitle() {
    if (this.type.equals(JavadocComponentType.TYPE) || this.type.equals(JavadocComponentType.MEMBER)) {
      String[] parts = this.qualifiedName.split("\\.");
      int classNameIndex = -1;

      for (int i = 0; i < parts.length; i++) {
        if (Character.isUpperCase(parts[i].charAt(0))) {
          classNameIndex = i;
          break;
        }
      }

      if (classNameIndex == -1) {
        return this.qualifiedName;
      }

      String className = String.join(".", Arrays.copyOfRange(parts, classNameIndex, parts.length - 1));
      String methodFieldName = parts[parts.length - 1];

      String displayName = className + (this.type == JavadocComponentType.TYPE ? "." : "#") + methodFieldName;
      if (displayName.startsWith(".")) {
        displayName = displayName.replaceFirst("\\.", "");
      }

      return displayName;
    } else if (this.type.equals(JavadocComponentType.PACKAGE)) {
      return this.packageName();
    }
    return this.name();
  }

  public String packageName() {
    String[] parts = this.qualifiedName.split("\\.");
    int classNameIndex = -1;

    for (int i = 0; i < parts.length; i++) {
      if (Character.isUpperCase(parts[i].charAt(0))) {
        classNameIndex = i;
        break;
      }
    }

    if (classNameIndex == -1) {
      return this.qualifiedName;
    }

    return String.join(".", Arrays.copyOfRange(parts, 0, classNameIndex));
  }

  public String urlDecoded() {
    return URLDecoder.decode(this.url, StandardCharsets.UTF_8);
  }

  public static JavadocItemPartial fromSearchableEntity(URI baseUrl, SearchableEntity searchableEntity) {
    String urlSearchableEntity = searchableEntity.url(baseUrl);
    return new JavadocItemPartial(urlSearchableEntity, searchableEntity.name(), searchableEntity.toString(), JavadocComponentType.fromSearchableEntity(searchableEntity));
  }
}
