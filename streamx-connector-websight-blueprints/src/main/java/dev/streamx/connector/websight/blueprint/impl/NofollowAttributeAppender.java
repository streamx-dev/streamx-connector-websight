package dev.streamx.connector.websight.blueprint.impl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class NofollowAttributeAppender {

  private static final Logger LOG = LoggerFactory.getLogger(NofollowAttributeAppender.class);

  private final boolean nofollowExternalLinks;
  private final Set<String> nofollowDisallowedHosts;

  NofollowAttributeAppender(boolean nofollowExternalLinks, Set<String> nofollowDisallowedHosts) {
    this.nofollowExternalLinks = nofollowExternalLinks;
    this.nofollowDisallowedHosts = nofollowDisallowedHosts;
  }

  String appendExternalLinks(String content) throws AddNofollowAttributeException {
    if (nofollowExternalLinks) {
      try {
        String escapedContent = escapeSpecialHtmlCharacters(content);
        Document document = Jsoup.parse(escapedContent);
        Elements links = document.select("a[href]");
        for (Element link : links) {
          String href = link.attr("href");
          if (isExternalLink(href) && isNofollowAllowedForHost(href)) {
            link.attr("rel", "nofollow");
          }
        }
        content = unescapeSpecialHtmlCharacters(Entities.unescape(document.outerHtml()));
      } catch (Exception e) {
        LOG.error("Error during content sanitization", e);
        throw new AddNofollowAttributeException("Can't append content with nofollow attribute.");
      }
    }
    return content;
  }

  private String escapeSpecialHtmlCharacters(String content) {
    return content.replaceAll("&([^;]+?);", "*&*$1;");
  }

  private String unescapeSpecialHtmlCharacters(String content) {
    return content.replaceAll("\\*&\\*([^;]+?);", "&$1;");
  }

  private boolean isExternalLink(String href) {
    return !StringUtils.startsWith(href, "/");
  }

  private boolean isNofollowAllowedForHost(String href) {
    try {
      URI uri = new URI(href);
      return !nofollowDisallowedHosts.contains(uri.getHost());
    } catch (URISyntaxException e) {
      LOG.debug("Cannot parse href {}: {}", href, e.getMessage());
      return true;
    }
  }

}
