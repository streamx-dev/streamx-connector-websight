package dev.streamx.connector.websight.blueprint.impl;

import dev.streamx.connector.websight.blueprint.PageDataService;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.engine.SlingRequestProcessor;
import org.apache.sling.servlethelpers.internalrequests.SlingInternalRequest;
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
  private static final String PAGES_PATH_REGEXP = "^/published/[^/]+/pages/.*$";

  @Reference
  private SlingRequestProcessor requestProcessor;

  private boolean shortenContentPaths;
  private String templatesPathPattern;

  private NofollowAttributeAppender nofollowAttributeAppender;

  @Activate
  @Modified
  private void activate(PageDataServiceConfig config) {
    shortenContentPaths = config.shorten_content_paths();
    templatesPathPattern = config.templates_pattern();
    nofollowAttributeAppender = new NofollowAttributeAppender(config.nofollow_external_links(),
        new HashSet<>(List.of(config.nofollow_external_links_disallowed_hosts())));
  }


  @Override
  public InputStream getStorageData(Resource resource) throws IOException {
    String response = new SlingInternalRequest(resource.getResourceResolver(), requestProcessor,
        resource.getPath()).withExtension("html").execute().getResponseAsString();

    response = appendNoFollowExternalLinks(resource, response);

    return wrapStreamIfNeeded(shortenContentPaths, resource.getPath(),
        new ByteArrayInputStream(response.getBytes()));
  }

  @Override
  public boolean isPage(String resourcePath) {
    return isMatchingPagesPathPattern(resourcePath) && !isMatchingPageTemplatePattern(resourcePath);
  }

  @Override
  public boolean isPageTemplate(String resourcePath) {
    return isMatchingPagesPathPattern(resourcePath) && isMatchingPageTemplatePattern(resourcePath);
  }

  private boolean isMatchingPageTemplatePattern(String resourcePath) {
    return resourcePath.matches(templatesPathPattern);
  }

  private boolean isMatchingPagesPathPattern(String resourcePath) {
    return resourcePath.matches(PAGES_PATH_REGEXP);
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

  private String appendNoFollowExternalLinks(Resource resource, String documentContent) {
    try {
      return nofollowAttributeAppender.appendExternalLinks(documentContent);
    } catch (AddNofollowAttributeException e) {
      LOG.error("Error during content sanitization for {}. Original content will be used.",
          resource.getPath(), e);
    }
    return documentContent;
  }
}
