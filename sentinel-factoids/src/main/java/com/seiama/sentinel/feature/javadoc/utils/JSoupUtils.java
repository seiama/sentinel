package com.seiama.sentinel.feature.javadoc.utils;

import com.overzealous.remark.Options;
import com.overzealous.remark.Remark;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class JSoupUtils {

  private static final Remark REMARK;
  private static final String LINK_SPACE_REGEX = "\"\"\"<[(.*?)]((.*))>\"\"\"";
  private static final Pattern FIX_NEW_LINES_PATTERN = Pattern.compile("\n{3,}");
  private static final Pattern FIX_SPACE_PATTERN = Pattern.compile("\\h");

  private static final Map<Character, Character> superscripts = new HashMap<>();
  private static final Map<Character, Character> subscripts = new HashMap<>();

  static {
    Options optsRemarkDiscord = Options.github();
    optsRemarkDiscord.inlineLinks = true;
    optsRemarkDiscord.fencedCodeBlocksWidth = 3;
    REMARK = new Remark(optsRemarkDiscord);

    // superscripts load
    String keysSuperscripts = "abcdefghijklmnopqrstuvwxyz0123456789+-=()";
    String valuesSuperscripts = "ᵃᵇᶜᵈᵉᶠᵍʰᶦʲᵏˡᵐⁿᵒᵖᑫʳˢᵗᵘᵛʷˣʸᶻ⁰¹²³⁴⁵⁶⁷⁸⁹⁺⁻⁼⁽⁾";
    for (int i = 0; i < keysSuperscripts.length(); i++) {
      superscripts.put(keysSuperscripts.charAt(i), valuesSuperscripts.charAt(i));
    }

    // additional superscripts
    superscripts.put('2', '\u00B2');
    superscripts.put('3', '\u00B3');
    superscripts.put('1', '\u00B9');
    superscripts.put('0', '\u2070');
    superscripts.put('i', '\u2071');
    superscripts.put('4', '\u2074');
    superscripts.put('5', '\u2075');
    superscripts.put('6', '\u2076');
    superscripts.put('7', '\u2077');
    superscripts.put('8', '\u2078');
    superscripts.put('9', '\u2079');
    superscripts.put('+', '\u207A');
    superscripts.put('-', '\u207B');
    superscripts.put('=', '\u207C');
    superscripts.put('(', '\u207D');
    superscripts.put(')', '\u207E');
    superscripts.put('n', '\u207F');
    superscripts.put('x', '\u02E3');
    superscripts.put('s', '\u02E2');
    superscripts.put('c', '\u1D9C');

    // subscripts load
    String keysSubscripts = "abcdefghijklmnopqrstuvwxyz0123456789+-=()";
    String valuesSubscripts = "ₐ₆꜀ₔₑբ₉ₕᵢⱼₖₗₘₙₒₚqᵣₛₜᵤᵥᵥᵥₓᵧ₂₀₁₂₃₄₅₆₇₈₉₊₋₌₍₎";
    for (int i = 0; i < keysSubscripts.length(); i++) {
      subscripts.put(keysSubscripts.charAt(i), valuesSubscripts.charAt(i));
    }

    // additional subscripts
    subscripts.put('0', '\u2080');
    subscripts.put('1', '\u2081');
    subscripts.put('2', '\u2082');
    subscripts.put('3', '\u2083');
    subscripts.put('4', '\u2084');
    subscripts.put('5', '\u2085');
    subscripts.put('6', '\u2086');
    subscripts.put('7', '\u2087');
    subscripts.put('8', '\u2088');
    subscripts.put('9', '\u2089');
    subscripts.put('+', '\u208A');
    subscripts.put('-', '\u208B');
    subscripts.put('=', '\u208C');
    subscripts.put('(', '\u208D');
    subscripts.put(')', '\u208E');
    subscripts.put('a', '\u2090');
    subscripts.put('e', '\u2091');
    subscripts.put('o', '\u2092');
    subscripts.put('x', '\u2093');
    subscripts.put('ə', '\u2094');
    subscripts.put('h', '\u2095');
    subscripts.put('k', '\u2096');
    subscripts.put('l', '\u2097');
    subscripts.put('m', '\u2098');
    subscripts.put('n', '\u2099');
    subscripts.put('p', '\u209A');
    subscripts.put('s', '\u209B');
    subscripts.put('t', '\u209C');

  }


  public static Document fetchDocument(String url) {
    try {
      return Jsoup.connect(url).followRedirects(true).get();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public static String formatText(Element element, String baseUrl) {
    element.setBaseUri(baseUrl);
    element.traverse((node, depth) -> {
      if (node instanceof Element nodeElement) {
        if (!nodeElement.tagName().equalsIgnoreCase("a")) {
          if (nodeElement.tagName().equalsIgnoreCase("sub")) {
            String text = replaceScriptCharactersOrNull(nodeElement.text(), subscripts);
            if (text != null) {
              nodeElement.text(text);
            }
          } else if (nodeElement.tagName().equalsIgnoreCase("sup")) {
            String text = replaceScriptCharactersOrNull(nodeElement.text(), superscripts);
            if (text != null) {
              nodeElement.text(text);
            }
          }

          return;
        }

        String href = node.absUrl("href");
        node.attr("href", href);
      }
    });
    return formatText(element.outerHtml(), element.baseUri());
  }

  public static String formatText(String docs, String url) {
    String markdown = REMARK.convertFragment(fixSpaces(docs), url);

    //remove unnecessary carriage return chars
    markdown = FIX_NEW_LINES_PATTERN.matcher(
      markdown.replace("\r", "") //fix codeblocks
        .replace("\n\n```", "\n\n```java")
    ).replaceAll("\n\n"); //remove too many newlines (max 2)
    return markdown.replace(LINK_SPACE_REGEX, "\"\"\"\\<[$1]($2)\\>\"\"\"");
  }

  private static String fixSpaces(String input) {
    return FIX_SPACE_PATTERN.matcher(input).replaceAll(" ");
  }

  public static String replaceScriptCharactersOrNull(String input, Map<Character, Character> mappings) {
    input = input.toLowerCase();
    StringBuilder result = new StringBuilder();

    for (char c : input.toCharArray()) {
      if (mappings.containsKey(c)) {
        result.append(mappings.get(c));
      } else {
        return null;
      }
    }
    return result.toString();
  }


}
