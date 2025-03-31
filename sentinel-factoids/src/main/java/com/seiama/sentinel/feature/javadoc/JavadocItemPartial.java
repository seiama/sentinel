package com.seiama.sentinel.feature.javadoc;

import com.seiama.sentinel.common.model.JavadocModel;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

  private static final Pattern CLASS_METHOD_PATTERN = Pattern.compile("^(.*?[A-Z][a-zA-Z0-9]*)\\.([a-zA-Z0-9_]+|[A-Z][a-zA-Z0-9]*)\\s*(\\(.*\\))?$");

  public String displayForChoice() {
    if (this.type.equals(JavadocModel.Complete.ComponentType.TYPE) || this.type.equals(JavadocModel.Complete.ComponentType.MEMBER)) {
      final String strDisplayName = this.displayName();
      String strFormat = strDisplayName + " [" + this.packageName() + "]";
      if (strFormat.length() > 100) {
        strFormat = strDisplayName + " [" + this.packageNameAbbreviation() + "]";
      }
      return strFormat;
    }
    return this.qualifiedName;
  }

  public String displayName() {
    if (this.type.equals(JavadocModel.Complete.ComponentType.TYPE) || this.type.equals(JavadocModel.Complete.ComponentType.MEMBER)) {
      final String packageName = this.packageName();
      final String strClassMethodParams = this.qualifiedName.replaceFirst(packageName, "").replaceFirst(".", "");

      final Matcher matcher = CLASS_METHOD_PATTERN.matcher(strClassMethodParams);
      String strFormatedClassMethodParams;

      if (matcher.find()) {
        final String strClass = matcher.group(1); // class
        final String strMethod = matcher.group(2); // method or constructor
        final String strParams = matcher.group(3); // params or null

        if (strParams == null) {
          strFormatedClassMethodParams = strClass + "#" + strMethod;
        } else {
          strFormatedClassMethodParams = strClass + "#" + strMethod + strParams;
        }
      } else {
        strFormatedClassMethodParams = strClassMethodParams;
      }

      return strFormatedClassMethodParams;
    }
    return this.qualifiedName;
  }

  public String displayTitle() {
    if (this.type.equals(JavadocModel.Complete.ComponentType.TYPE) || this.type.equals(JavadocModel.Complete.ComponentType.MEMBER)) {
      return this.displayName();
    } else if (this.type.equals(JavadocModel.Complete.ComponentType.PACKAGE)) {
      return this.packageName();
    }
    return this.name();
  }

  public String packageNameAbbreviation() {
    final String[] parts = this.packageName().split("\\.");
    final StringBuilder abbreviatedName = new StringBuilder();
    for (final String part : parts) {
      abbreviatedName.append(part.charAt(0));
      abbreviatedName.append(".");
    }

    return abbreviatedName.substring(0, abbreviatedName.length() - 1);
  }

  public String packageName() {
    final String[] parts = this.qualifiedName.split("\\.");
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

  public static JavadocItemPartial fromSearchableEntity(final URI baseUrl, final SearchableEntity searchableEntity) {
    final String urlSearchableEntity = searchableEntity.url(baseUrl);
    return new JavadocItemPartial(urlSearchableEntity, searchableEntity.name(), searchableEntity.toString(), fromSearchableEntity(searchableEntity));
  }

  private static JavadocModel.Complete.ComponentType fromSearchableEntity(final SearchableEntity searchableEntity) {
    if (searchableEntity instanceof Type) {
      return JavadocModel.Complete.ComponentType.TYPE;
    } else if (searchableEntity instanceof net.maisikoleni.javadoc.entities.Module) {
      return JavadocModel.Complete.ComponentType.MODULE;
    } else if (searchableEntity instanceof Member) {
      return JavadocModel.Complete.ComponentType.MEMBER;
    } else if (searchableEntity instanceof Tag) {
      return JavadocModel.Complete.ComponentType.TAG;
    } else if (searchableEntity instanceof Package) {
      return JavadocModel.Complete.ComponentType.PACKAGE;
    }
    return JavadocModel.Complete.ComponentType.ALL;
  }
}
