package com.seiama.sentinel.feature.javadoc;

import net.maisikoleni.javadoc.entities.Member;
import net.maisikoleni.javadoc.entities.Module;
import net.maisikoleni.javadoc.entities.Package;
import net.maisikoleni.javadoc.entities.SearchableEntity;
import net.maisikoleni.javadoc.entities.Tag;
import net.maisikoleni.javadoc.entities.Type;

public enum JavadocComponentType {
  ALL("all"),
  MODULE("module"),
  PACKAGE("package"),
  TYPE("type"),
  MEMBER("member"),
  TAG("tag");

  private final String name;

  JavadocComponentType(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  public static JavadocComponentType fromString(String name) {
    for (JavadocComponentType type : values()) {
      if (type.name.equalsIgnoreCase(name)) {
        return type;
      }
    }
    return ALL;
  }

  public static JavadocComponentType fromSearchableEntity(SearchableEntity searchableEntity) {
    if (searchableEntity instanceof Type) {
      return TYPE;
    } else if(searchableEntity instanceof Module) {
      return MODULE;
    } else if(searchableEntity instanceof Member) {
      return MEMBER;
    } else if (searchableEntity instanceof Tag) {
      return TAG;
    } else if (searchableEntity instanceof Package) {
      return PACKAGE;
    }
    return ALL;
  }

}
