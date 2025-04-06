package com.seiama.sentinel.feature.javadoc.utils;

import com.overzealous.remark.Options;
import com.overzealous.remark.Remark;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("checkstyle:AvoidEscapedUnicodeCharacters")
@NullMarked
public final class JavaDocUtils {

  private static final Remark REMARK;
  private static final String LINK_SPACE_REGEX = "\"\"\"<[(.*?)]((.*))>\"\"\"";
  private static final Pattern FIX_NEW_LINES_PATTERN = Pattern.compile("\n{3,}");
  private static final Pattern FIX_SPACE_PATTERN = Pattern.compile("\\h");

  private static final Map<Character, Character> SUPERSCRIPTS = new HashMap<>();
  private static final Map<Character, Character> SUBSCRIPTS = new HashMap<>();

  static {
    final Options optsRemarkDiscord = Options.github();
    optsRemarkDiscord.inlineLinks = true;
    optsRemarkDiscord.fencedCodeBlocksWidth = 3;
    REMARK = new Remark(optsRemarkDiscord);

    // superscripts load
    final String keysSuperscripts = "abcdefghijklmnopqrstuvwxyz0123456789+-=()";
    final String valuesSuperscripts = "ᵃᵇᶜᵈᵉᶠᵍʰᶦʲᵏˡᵐⁿᵒᵖᑫʳˢᵗᵘᵛʷˣʸᶻ⁰¹²³⁴⁵⁶⁷⁸⁹⁺⁻⁼⁽⁾";
    for (int i = 0; i < keysSuperscripts.length(); i++) {
      SUPERSCRIPTS.put(keysSuperscripts.charAt(i), valuesSuperscripts.charAt(i));
    }

    // additional superscripts
    SUPERSCRIPTS.put('2', '\u00B2');
    SUPERSCRIPTS.put('3', '\u00B3');
    SUPERSCRIPTS.put('1', '\u00B9');
    SUPERSCRIPTS.put('0', '\u2070');
    SUPERSCRIPTS.put('i', '\u2071');
    SUPERSCRIPTS.put('4', '\u2074');
    SUPERSCRIPTS.put('5', '\u2075');
    SUPERSCRIPTS.put('6', '\u2076');
    SUPERSCRIPTS.put('7', '\u2077');
    SUPERSCRIPTS.put('8', '\u2078');
    SUPERSCRIPTS.put('9', '\u2079');
    SUPERSCRIPTS.put('+', '\u207A');
    SUPERSCRIPTS.put('-', '\u207B');
    SUPERSCRIPTS.put('=', '\u207C');
    SUPERSCRIPTS.put('(', '\u207D');
    SUPERSCRIPTS.put(')', '\u207E');
    SUPERSCRIPTS.put('n', '\u207F');
    SUPERSCRIPTS.put('x', '\u02E3');
    SUPERSCRIPTS.put('s', '\u02E2');
    SUPERSCRIPTS.put('c', '\u1D9C');

    // subscripts load
    final String keysSubscripts = "abcdefghijklmnopqrstuvwxyz0123456789+-=()";
    final String valuesSubscripts = "ₐ₆꜀ₔₑբ₉ₕᵢⱼₖₗₘₙₒₚqᵣₛₜᵤᵥᵥᵥₓᵧ₂₀₁₂₃₄₅₆₇₈₉₊₋₌₍₎";
    for (int i = 0; i < keysSubscripts.length(); i++) {
      SUBSCRIPTS.put(keysSubscripts.charAt(i), valuesSubscripts.charAt(i));
    }

    // additional subscripts
    SUBSCRIPTS.put('0', '\u2080');
    SUBSCRIPTS.put('1', '\u2081');
    SUBSCRIPTS.put('2', '\u2082');
    SUBSCRIPTS.put('3', '\u2083');
    SUBSCRIPTS.put('4', '\u2084');
    SUBSCRIPTS.put('5', '\u2085');
    SUBSCRIPTS.put('6', '\u2086');
    SUBSCRIPTS.put('7', '\u2087');
    SUBSCRIPTS.put('8', '\u2088');
    SUBSCRIPTS.put('9', '\u2089');
    SUBSCRIPTS.put('+', '\u208A');
    SUBSCRIPTS.put('-', '\u208B');
    SUBSCRIPTS.put('=', '\u208C');
    SUBSCRIPTS.put('(', '\u208D');
    SUBSCRIPTS.put(')', '\u208E');
    SUBSCRIPTS.put('a', '\u2090');
    SUBSCRIPTS.put('e', '\u2091');
    SUBSCRIPTS.put('o', '\u2092');
    SUBSCRIPTS.put('x', '\u2093');
    SUBSCRIPTS.put('ə', '\u2094');
    SUBSCRIPTS.put('h', '\u2095');
    SUBSCRIPTS.put('k', '\u2096');
    SUBSCRIPTS.put('l', '\u2097');
    SUBSCRIPTS.put('m', '\u2098');
    SUBSCRIPTS.put('n', '\u2099');
    SUBSCRIPTS.put('p', '\u209A');
    SUBSCRIPTS.put('s', '\u209B');
    SUBSCRIPTS.put('t', '\u209C');

  }

  public static Document fetchDocument(final String url) {
    try {
      return Jsoup.connect(url).followRedirects(true).get();
    } catch (final IOException exception) {
      throw new RuntimeException(exception);
    }
  }

  public static String formatText(final Element element, final String baseUrl) {
    element.setBaseUri(baseUrl);
    element.traverse((node, depth) -> {
      if (node instanceof Element nodeElement) {
        if (!nodeElement.tagName().equalsIgnoreCase("a")) {
          if (nodeElement.tagName().equalsIgnoreCase("sub")) {
            final String text = replaceScriptCharactersOrNull(nodeElement.text(), SUBSCRIPTS);
            if (text != null) {
              nodeElement.text(text);
            }
          } else if (nodeElement.tagName().equalsIgnoreCase("sup")) {
            final String text = replaceScriptCharactersOrNull(nodeElement.text(), SUPERSCRIPTS);
            if (text != null) {
              nodeElement.text(text);
            }
          }

          return;
        }

        final String href = node.absUrl("href");
        node.attr("href", href);
      }
    });
    return formatText(element.outerHtml(), element.baseUri());
  }

  public static String formatText(final String docs, final String url) {
    String markdown = REMARK.convertFragment(fixSpaces(docs), url);

    // remove unnecessary carriage return chars
    markdown = FIX_NEW_LINES_PATTERN.matcher(
      markdown.replace("\r", "") // fix codeblocks
        .replace("\n\n```", "\n\n```java")
    ).replaceAll("\n\n"); // remove too many newlines (max 2)
    return markdown.replace(LINK_SPACE_REGEX, "\"\"\"\\<[$1]($2)\\>\"\"\"");
  }

  private static String fixSpaces(final String input) {
    return FIX_SPACE_PATTERN.matcher(input).replaceAll(" ");
  }

  @Nullable
  public static String replaceScriptCharactersOrNull(String input, final Map<Character, Character> mappings) {
    input = input.toLowerCase(Locale.ROOT);
    final StringBuilder result = new StringBuilder();

    for (int i = 0; i < input.length(); i++) {
      final char c = input.charAt(i);
      if (mappings.containsKey(c)) {
        result.append(mappings.get(c));
      } else {
        return null;
      }
    }
    return result.toString();
  }

  private JavaDocUtils() {
  }

}
