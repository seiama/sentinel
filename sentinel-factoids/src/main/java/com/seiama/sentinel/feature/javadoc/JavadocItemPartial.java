package com.seiama.sentinel.feature.javadoc;

import com.seiama.sentinel.common.model.JavadocModel;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import net.maisikoleni.javadoc.entities.Member;
import net.maisikoleni.javadoc.entities.Package;
import net.maisikoleni.javadoc.entities.SearchableEntity;
import net.maisikoleni.javadoc.entities.Tag;
import net.maisikoleni.javadoc.entities.Type;

public record JavadocItemPartial(
  String url,
  String name,
  String qualifiedName,
  JavadocModel.Complete.ComponentType type
) {

  public String displayName() {
    if (this.type.equals(JavadocModel.Complete.ComponentType.TYPE) || this.type.equals(JavadocModel.Complete.ComponentType.MEMBER)) {
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

      return className + (this.type == JavadocModel.Complete.ComponentType.TYPE ? "." : "#") + methodFieldName + " [" + packageName + "]";
    }
    return this.qualifiedName;
  }

  public String displayTitle() {
    if (this.type.equals(JavadocModel.Complete.ComponentType.TYPE) || this.type.equals(JavadocModel.Complete.ComponentType.MEMBER)) {
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

      String displayName = className + (this.type == JavadocModel.Complete.ComponentType.TYPE ? "." : "#") + methodFieldName;
      if (displayName.startsWith(".")) {
        displayName = displayName.replaceFirst("\\.", "");
      }

      return displayName;
    } else if (this.type.equals(JavadocModel.Complete.ComponentType.PACKAGE)) {
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
    return new JavadocItemPartial(urlSearchableEntity, searchableEntity.name(), searchableEntity.toString(), fromSearchableEntity(searchableEntity));
  }

  private static JavadocModel.Complete.ComponentType fromSearchableEntity(SearchableEntity searchableEntity) {
    if (searchableEntity instanceof Type) {
      return JavadocModel.Complete.ComponentType.TYPE;
    } else if(searchableEntity instanceof net.maisikoleni.javadoc.entities.Module) {
      return JavadocModel.Complete.ComponentType.MODULE;
    } else if(searchableEntity instanceof Member) {
      return JavadocModel.Complete.ComponentType.MEMBER;
    } else if (searchableEntity instanceof Tag) {
      return JavadocModel.Complete.ComponentType.TAG;
    } else if (searchableEntity instanceof Package) {
      return JavadocModel.Complete.ComponentType.PACKAGE;
    }
    return JavadocModel.Complete.ComponentType.ALL;
  }
}
