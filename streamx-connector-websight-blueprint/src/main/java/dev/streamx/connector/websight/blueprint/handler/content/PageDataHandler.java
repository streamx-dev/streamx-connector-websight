package dev.streamx.connector.websight.blueprint.handler.content;

import dev.streamx.connector.websight.blueprint.ResourceResolverProvider;
import dev.streamx.connector.websight.blueprint.reference.relay.model.PageModel;
import dev.streamx.sling.connector.PublicationHandler;
import dev.streamx.sling.connector.PublishData;
import dev.streamx.sling.connector.UnpublishData;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
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
import pl.ds.websight.publishing.framework.PublishException;

@Component
@Designate(ocd = PageDataHandlerConfig.class)
public class PageDataHandler implements PublicationHandler<PageModel> {

  private static final Logger LOG = LoggerFactory.getLogger(PageDataHandler.class);
  private static final String PAGES_PATH_REGEXP = "^/published/[^/]+/pages/.*$";

  @Reference
  private SlingRequestProcessor requestProcessor;

  @Reference
  private ResourceResolverProvider resourceResolverProvider;

  private boolean shortenContentPaths;
  private boolean nofollowExternalLinks;
  private HashSet<String> nofollowDisallowedHosts;
  private String pagesPublicationChannel;
  private String templatesPublicationChannel;
  private String templatesPathPattern;

  @Activate
  @Modified
  private void activate(PageDataHandlerConfig config) {
    shortenContentPaths = config.shorten_content_paths();
    nofollowExternalLinks = config.nofollow_external_links();
    nofollowDisallowedHosts = new HashSet<>(
        List.of(config.nofollow_external_links_disallowed_hosts()));
    pagesPublicationChannel = config.pages_publication_channel();
    templatesPublicationChannel = config.templates_publication_channel();
    templatesPathPattern = config.templates_pattern();
  }

  @Override
  public String getId() {
    return "WebSight-Pages-Handler";
  }

  @Override
  public boolean canHandle(String resourcePath) {
    return resourcePath.matches(PAGES_PATH_REGEXP);
  }

  @Override
  public PublishData<PageModel> getPublishData(String resourcePath) {
    try (ResourceResolver resourceResolver = resourceResolverProvider.getResourceResolver()) {
      Resource resource = resourceResolver.getResource(resourcePath);
      if (resource != null) {
        PageModel page = resolveData(resource);
        return new PublishData<>(getStoragePath(resourcePath),
            getPublicationChannel(resource.getPath()), PageModel.class, page);
      } else {
        LOG.info("Cannot prepare publish data for {}. Resource doesn't exist.", resourcePath);
      }
    } catch (PublishException e) {
      LOG.error("Cannot prepare publish data for {}.", resourcePath, e);
    } catch (LoginException e) {
      LOG.error("Cannot get resource resolver.");
    }
    return null;
  }

  @Override
  public UnpublishData<PageModel> getUnpublishData(String resourcePath) {
    return new UnpublishData<>(getStoragePath(resourcePath), getPublicationChannel(resourcePath),
        PageModel.class);
  }

  private PageModel resolveData(Resource resource) throws PublishException {
    try {
      return getPublicationData(resource);
    } catch (IOException e) {
      LOG.warn("IOException occurred when storing data for resource {}", resource.getPath());
      throw new PublishException(String.format("Cannot read resource %s data", resource.getPath()),
          e);
    }
  }

  private String getStoragePath(String resourcePath) {
    return resourcePath + ".html";
  }

  private String getPublicationChannel(String resourcePath) {
    if (resourcePath.matches(templatesPathPattern)) {
      return templatesPublicationChannel;
    }
    return pagesPublicationChannel;
  }

  private PageModel getPublicationData(Resource resource) throws IOException {
    return new PageModel(ByteBuffer.wrap(getStorageData(resource).readAllBytes()));
  }


  public InputStream getStorageData(Resource resource) throws IOException {
    String response = new SlingInternalRequest(resource.getResourceResolver(), requestProcessor,
        resource.getPath()).withExtension("html").execute().getResponseAsString();

    response = replaceNofollowExternalLinksIfNeeded(resource, response);

    return wrapStreamIfNeeded(shortenContentPaths, resource.getPath(),
        new ByteArrayInputStream(response.getBytes()));
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
