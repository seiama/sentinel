package com.seiama.sentinel.feature.javadoc;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.maisikoleni.javadoc.db.JavadocIndexes;
import net.maisikoleni.javadoc.entities.JavadocIndex;

public class BasicJavadocIndexes implements JavadocIndexes {

  private final Map<URI, JavadocIndex> indexesByUri = new ConcurrentHashMap<>();

  @Override
  public JavadocIndex getIndexByBaseUrl(URI baseUrl, Supplier<JavadocIndex> alternativeSource) {
    return indexesByUri.computeIfAbsent(baseUrl, url -> alternativeSource.get());
  }

  public Map<URI, JavadocIndex> getRawIndexesByUriMap() {
    return indexesByUri;
  }
}
