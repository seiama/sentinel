package com.seiama.sentinel.feature.javadoc.classes;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.maisikoleni.javadoc.db.JavadocIndexes;
import net.maisikoleni.javadoc.entities.JavadocIndex;
import org.jspecify.annotations.NullMarked;

@SuppressWarnings("checkstyle:MethodName")
@NullMarked
public class BasicJavadocIndexes implements JavadocIndexes {

  private final Map<URI, JavadocIndex> indexesByUri = new ConcurrentHashMap<>();

  @Override
  public JavadocIndex getIndexByBaseUrl(final URI baseUrl, final Supplier<JavadocIndex> alternativeSource) {
    return this.indexesByUri.computeIfAbsent(baseUrl, url -> alternativeSource.get());
  }

  public Map<URI, JavadocIndex> getRawIndexesByUriMap() {
    return this.indexesByUri;
  }
}
