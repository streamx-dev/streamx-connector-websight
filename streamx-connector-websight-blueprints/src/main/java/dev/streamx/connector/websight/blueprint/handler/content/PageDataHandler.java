package dev.streamx.connector.websight.blueprint.handler.content;

import dev.streamx.connector.websight.blueprint.PageDataService;
import dev.streamx.connector.websight.blueprint.ResourceResolverProvider;
import dev.streamx.connector.websight.blueprint.model.PageModel;
import dev.streamx.sling.connector.PublicationHandler;
import dev.streamx.sling.connector.PublishData;
import dev.streamx.sling.connector.UnpublishData;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
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
  private ResourceResolverProvider resourceResolverProvider;

  @Reference
  private PageDataService pageDataService;

  private String publicationChannel;
  private boolean enabled;

  @Activate
  @Modified
  private void activate(PageDataHandlerConfig config) {
    publicationChannel = config.publication_channel();
    enabled = config.enabled();
  }

  @Override
  public String getId() {
    return "WebSight-Pages-Handler";
  }

  @Override
  public boolean canHandle(String resourcePath) {
    return enabled && resourcePath.matches(PAGES_PATH_REGEXP) && pageDataService.isPage(
        resourcePath);
  }

  @Override
  public PublishData<PageModel> getPublishData(String resourcePath) {
    try (ResourceResolver resourceResolver = resourceResolverProvider.getResourceResolver()) {
      Resource resource = resourceResolver.getResource(resourcePath);
      if (resource != null) {
        PageModel page = resolveData(resource);
        return new PublishData<>(getStoragePath(resourcePath), publicationChannel, PageModel.class,
            page);
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
    return new UnpublishData<>(getStoragePath(resourcePath), publicationChannel, PageModel.class);
  }

  private PageModel resolveData(Resource resource) throws PublishException {
    try (InputStream dataStream = getStorageData(resource)) {
      return new PageModel(ByteBuffer.wrap(dataStream.readAllBytes()));
    } catch (IOException e) {
      LOG.warn("IOException occurred when storing data for resource {}", resource.getPath());
      throw new PublishException(String.format("Cannot read resource %s data", resource.getPath()),
          e);
    }
  }

  private String getStoragePath(String resourcePath) {
    return resourcePath + ".html";
  }


  public InputStream getStorageData(Resource resource) throws IOException {
    return pageDataService.getStorageData(resource);
  }
}
