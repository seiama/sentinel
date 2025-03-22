package com.seiama.sentinel.feature.javadoc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class JavadocSearch {

  private final String url;
  private final Document document;
  private final List<JavadocItemPartial> elements = new ArrayList<>();

  public JavadocSearch(final String url) {
    this.url = url;
    this.document = this.fetchDocument(this.url.concat("allclasses-index.html"));
    Elements indexItemElements = this.document.select("a[href][title]");
    for (Element indexItemElement : indexItemElements) {
      final String indexHref = indexItemElement.attr("href");
      final String urlDocs = (indexHref.startsWith("https") ? indexHref : url + indexHref);
      final String jdElementName = indexItemElement.text();
      final String jdElementType = Arrays.stream(indexItemElement.attr("title").split("\\s+")).findFirst().orElse("");

      Pattern pattern = Pattern.compile("^(?:https?://[^/]+/)?(?:[^/]+/\\d+(?:\\.\\d+)*/)?([^/]+(?:/[^/]+)*)/[^/]+\\.html$");
      Matcher matcher = pattern.matcher(indexHref);
      final String jdElementPackage = matcher.find() ? matcher.group(1).replaceAll("/", ".") : "---";
      this.elements.add(new JavadocItemPartial(urlDocs, jdElementType, jdElementPackage, jdElementName));
    }
  }

  private Document fetchDocument(String url) {
    try {
      return Jsoup.connect(url).followRedirects(true).get();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<JavadocItemPartial> search(final String keyword, final @Nullable JavadocElementType type) {
    return this.elements.stream().filter(javadocItemPartial -> javadocItemPartial.name().toLowerCase(Locale.ROOT).contains(keyword) && (type == null || type == JavadocElementType.UNKNOW || javadocItemPartial.type().toLowerCase(Locale.ROOT).contains(type.getName()))).toList();
  }

  public JavadocItem getJavadocItem(final JavadocItemPartial javadocItemPartial) {
    Document document = this.fetchDocument(javadocItemPartial.url());
    Element documentClassDescElement = document.selectFirst("#class-description");
    final String jdElementDescription = Optional.ofNullable(document.selectFirst(".block")).map(Element::wholeText).orElse("");
    final boolean jdElementDeprecated = documentClassDescElement != null && documentClassDescElement.selectFirst(".deprecated-label") != null;
    String jdElementDeprecatedMessage = "";
    if (jdElementDeprecated) {
      Element docClassDeprecatedMessage = documentClassDescElement.selectFirst(".deprecation-comment");
      if (docClassDeprecatedMessage != null) {
        jdElementDeprecatedMessage = docClassDeprecatedMessage.wholeText();
      }
    }
    return new JavadocItem(javadocItemPartial.url(), javadocItemPartial.type(), javadocItemPartial.packagePath(), javadocItemPartial.name(), jdElementDescription, jdElementDeprecated, jdElementDeprecatedMessage);
  }
}
