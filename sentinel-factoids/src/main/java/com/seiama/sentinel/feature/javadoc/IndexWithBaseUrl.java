package com.seiama.sentinel.feature.javadoc;

import java.net.URI;
import net.maisikoleni.javadoc.entities.JavadocIndex;

public record IndexWithBaseUrl(URI baseUrl, JavadocIndex index) {
}
