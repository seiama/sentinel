package com.seiama.sentinel.feature.javadoc.classes;

import java.net.URI;
import net.maisikoleni.javadoc.entities.JavadocIndex;

public record IndexWithBaseUrl(URI baseUrl, JavadocIndex index) {
}
