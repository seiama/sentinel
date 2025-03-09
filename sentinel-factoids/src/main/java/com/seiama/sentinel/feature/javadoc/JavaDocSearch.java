package com.seiama.sentinel.feature.javadoc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class JavaDocSearch {

  private final String url;
  private final Document document;
  private final List<JavaDocItemPartial> elements = new ArrayList<>();

  public JavaDocSearch(final String url) {
    this.url = url;
    this.document = this.fetchIndexsDocument(url);
    Elements indexItemElements = this.document.select("a[href][title]");
    for (Element indexItemElement : indexItemElements) {
      final String indexHref = indexItemElement.attr("href");
      final String urlDocs = (indexHref.startsWith("https") ? indexHref : url + indexHref);
      final String jdElementName = indexItemElement.text();
      final String jdElementType = indexItemElement.attr("title").split(" ")[0];

      Pattern pattern = Pattern.compile("^(?:https?://[^/]+/)?(?:[^/]+/\\d+(?:\\.\\d+)*/)?([^/]+(?:/[^/]+)*)/[^/]+\\.html$");
      Matcher matcher = pattern.matcher(indexHref);
      final String jdElementPackage = (matcher.find()) ? matcher.group(1).replaceAll("/", ".") : "---";
      this.elements.add(new JavaDocItemPartial(urlDocs, jdElementName, jdElementType, jdElementPackage));
    }
  }

  private Document fetchIndexsDocument(String url) {
    try {
      return Jsoup.connect(url).followRedirects(true).get();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<JavaDocItemPartial> search(final String keyword, final @Nullable JavaDocElementType type) {
    return this.elements.stream().filter(javaDocItemPartial -> javaDocItemPartial.name().toLowerCase().contains(keyword) && (type == null || javaDocItemPartial.type().toLowerCase().contains(type.getName()))).toList();
  }
}
