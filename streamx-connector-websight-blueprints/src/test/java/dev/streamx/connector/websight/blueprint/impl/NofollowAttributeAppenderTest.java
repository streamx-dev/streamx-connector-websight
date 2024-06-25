package dev.streamx.connector.websight.blueprint.impl;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

class NofollowAttributeAppenderTest {

  @Test
  void shouldAddNofollowToAllExternalLinks() throws IOException, AddNofollowAttributeException {
    NofollowAttributeAppender appender = new NofollowAttributeAppender(true,
        Collections.emptySet());
    InputStream pageStream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("dev/streamx/connector/websight/blueprint/impl/pages/page.html");
    String page = IOUtils.toString(pageStream, StandardCharsets.UTF_8);

    String appendedPage = appender.appendExternalLinks(page);

    HtmlDocumentAssert.assertThat(appendedPage)
        .containsNoFollowLink("https://example.com/link-1")
        .containsNoFollowLink("https://example.com/link-2")
        .containsNoFollowLink("https://other-example.com/link-1")
        .containsNoFollowLink("https://other-example.com/link-2")
        .containsLinkWithoutRel("/internal/link")
        .containsLinkWithoutRel("#anchorLink");
  }

  @Test
  void shouldNotAddNofollowToAnyLinks() throws IOException, AddNofollowAttributeException {
    NofollowAttributeAppender appender = new NofollowAttributeAppender(false,
        Collections.emptySet());
    InputStream pageStream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("dev/streamx/connector/websight/blueprint/impl/pages/page.html");
    String page = IOUtils.toString(pageStream, StandardCharsets.UTF_8);

    String appendedPage = appender.appendExternalLinks(page);

    HtmlDocumentAssert.assertThat(appendedPage)
        .containsLinkWithoutRel("https://example.com/link-1")
        .containsLinkWithoutRel("https://example.com/link-2")
        .containsLinkWithoutRel("https://other-example.com/link-1")
        .containsLinkWithoutRel("https://other-example.com/link-2")
        .containsLinkWithoutRel("/internal/link")
        .containsLinkWithoutRel("#anchorLink");
  }

  @Test
  void shouldAddNofollowOnlyToExamplePageLinks() throws IOException, AddNofollowAttributeException {
    NofollowAttributeAppender appender = new NofollowAttributeAppender(true,
        Collections.singleton("other-example.com"));
    InputStream pageStream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("dev/streamx/connector/websight/blueprint/impl/pages/page.html");
    String page = IOUtils.toString(pageStream, StandardCharsets.UTF_8);

    String appendedPage = appender.appendExternalLinks(page);

    HtmlDocumentAssert.assertThat(appendedPage)
        .containsNoFollowLink("https://example.com/link-1")
        .containsNoFollowLink("https://example.com/link-2")
        .containsLinkWithoutRel("https://other-example.com/link-1")
        .containsLinkWithoutRel("https://other-example.com/link-2");
  }

  @Test
  void shouldNotBreakTemplateTags() throws IOException, AddNofollowAttributeException {
    NofollowAttributeAppender appender = new NofollowAttributeAppender(true,
        Collections.singleton("other-example.com"));
    InputStream pageStream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("dev/streamx/connector/websight/blueprint/impl/pages/template.html");
    String page = IOUtils.toString(pageStream, StandardCharsets.UTF_8);

    String appendedPage = appender.appendExternalLinks(page);

    HtmlDocumentAssert.assertThat(appendedPage)
        .containsText("{% for image in images %}")
        .containsText("{% if loop.index < 5 %}")
        .containsText("{% endif %}")
        .containsText("{% endfor %}");
  }

  @Test
  void shouldNotBreakSpacialCharacters() throws IOException, AddNofollowAttributeException {
    NofollowAttributeAppender appender = new NofollowAttributeAppender(true,
        Collections.singleton("other-example.com"));
    InputStream pageStream = Thread.currentThread().getContextClassLoader()
        .getResourceAsStream("dev/streamx/connector/websight/blueprint/impl/pages/page.html");
    String page = IOUtils.toString(pageStream, StandardCharsets.UTF_8);

    String appendedPage = appender.appendExternalLinks(page);

    HtmlDocumentAssert.assertThat(appendedPage)
        .containsText("Special character &lt;");
    HtmlDocumentAssert.assertThat(appendedPage)
        .containsText("Longest Special character: &CounterClockwiseContourIntegral;");
  }

}