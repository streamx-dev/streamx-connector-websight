package dev.streamx.connector.websight.blueprint.impl;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

class HtmlDocumentAssert {

  private final Document document;
  private final String documentContent;

  private HtmlDocumentAssert(String documentContent, Document document) {
    this.documentContent = documentContent;
    this.document = document;
  }

  static HtmlDocumentAssert assertThat(String htmlDocumentContent) {
    return new HtmlDocumentAssert(htmlDocumentContent, Jsoup.parse(htmlDocumentContent));
  }

  HtmlDocumentAssert containsText(String text) {
    Assertions.assertThat(documentContent)
        .overridingErrorMessage(() -> "Document should contain: " + text)
        .contains(text);
    return this;
  }

  HtmlDocumentAssert containsNoFollowLink(String link) {
    List<String> rel = document.select("a[href=\"" + link +"\"]")
        .stream()
        .map(element -> element.attributes().get("rel"))
        .toList();

    Assertions.assertThat(rel)
        .overridingErrorMessage(() -> link + " should have rel=\"nofollow\" attribute.")
        .containsExactly("nofollow");

    return this;
  }

  HtmlDocumentAssert containsLinkWithoutRel(String link) {
    List<String> rel = document.select("a[href=\"" + link +"\"]")
        .stream()
        .map(element -> element.attributes().get("rel"))
        .toList();

    Assertions.assertThat(rel)
        .overridingErrorMessage(() -> link + " shouldn't have rel=\"nofollow\" attribute.")
        .containsExactly("");

    return this;
  }

}
