package dev.streamx.connector.websight.blueprint.impl;

import dev.streamx.connector.websight.blueprint.PageDataService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.engine.SlingRequestProcessor;
import org.apache.sling.servlethelpers.internalrequests.SlingInternalRequest;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Designate(ocd = PageDataServiceConfig.class)
public class PageDataServiceImpl implements PageDataService {

  private static final Logger LOG = LoggerFactory.getLogger(PageDataServiceImpl.class);

  @Reference
  private SlingRequestProcessor requestProcessor;

  private boolean shortenContentPaths;
  private boolean nofollowExternalLinks;
  private HashSet<String> nofollowDisallowedHosts;
  private String templatesPathPattern;

  @Activate
  @Modified
  private void activate(PageDataServiceConfig config) {
    shortenContentPaths = config.shorten_content_paths();
    nofollowExternalLinks = config.nofollow_external_links();
    nofollowDisallowedHosts = new HashSet<>(
        List.of(config.nofollow_external_links_disallowed_hosts()));
    templatesPathPattern = config.templates_pattern();
  }


  @Override
  public InputStream getStorageData(Resource resource) throws IOException {
    String response = new SlingInternalRequest(resource.getResourceResolver(), requestProcessor,
        resource.getPath()).withExtension("html").execute().getResponseAsString();

    response = replaceNofollowExternalLinksIfNeeded(resource, response);

    return wrapStreamIfNeeded(shortenContentPaths, resource.getPath(),
        new ByteArrayInputStream(response.getBytes()));
  }

  @Override
  public boolean isPage(String resourcePath) {
    return !isPageTemplate(resourcePath);
  }

  @Override
  public boolean isPageTemplate(String resourcePath) {
    return resourcePath.matches(templatesPathPattern);
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

  private InputStream wrapStreamIfNeeded(boolean shortenPaths, String path, InputStream input) {
    if (shortenPaths && path.startsWith("/published")) {
      final String[] elements = path.split("/");
      final String spaceName = elements.length >= 2 ? elements[2] : null;
      if (spaceName != null) {
        final byte[] pagesPath = ("/published/" + spaceName + "/pages").getBytes();
        final byte[] contentPath = ("/published/" + spaceName).getBytes();
        // Replace pages paths
        final InputStream replacePages = new ReplacingInputStream(input, pagesPath, null);
        // Replace generic paths
        return new ReplacingInputStream(replacePages, contentPath, null);
      }
    }
    return input;
  }

  private String replaceNofollowExternalLinksIfNeeded(Resource resource, String response) {
    if (nofollowExternalLinks) {
      try {
        Document document = Jsoup.parse(response, "UTF-8");
        Elements links = document.select("a[href]");
        for (Element link : links) {
          String href = link.attr("href");
          if (!StringUtils.startsWith(href, "/") && isNofollowAllowedForHost(href)) {
            link.attr("rel", "nofollow");
          }
        }
        response = document.outerHtml();
      } catch (Exception e) {
        LOG.error("Error during content sanitization for {}. Original content will be used.",
            resource.getPath());
        LOG.debug("Error details", e);
      }
    }
    return response;
  }
}
