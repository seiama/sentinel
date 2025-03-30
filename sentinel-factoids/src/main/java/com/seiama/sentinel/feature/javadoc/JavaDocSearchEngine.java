package com.seiama.sentinel.feature.javadoc;

import net.maisikoleni.javadoc.search.SearchEngine;
import net.maisikoleni.javadoc.service.JavadocImpl;

public record JavaDocSearchEngine(JavadocImpl javadoc, SearchEngine searchEngine) {
}
