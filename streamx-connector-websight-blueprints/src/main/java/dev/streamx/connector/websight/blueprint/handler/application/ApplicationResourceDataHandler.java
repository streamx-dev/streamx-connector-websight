package dev.streamx.connector.websight.blueprint.handler.application;

import dev.streamx.connector.websight.blueprint.ResourceResolverProvider;
import dev.streamx.connector.websight.blueprint.model.WebResourceModel;
import dev.streamx.sling.connector.PublicationHandler;
import dev.streamx.sling.connector.PublishData;
import dev.streamx.sling.connector.UnpublishData;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
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
@Designate(ocd = ApplicationResourceDataConfig.class)
public class ApplicationResourceDataHandler implements PublicationHandler<WebResourceModel> {

  private static final Logger LOG = LoggerFactory.getLogger(ApplicationResourceDataHandler.class);

  private String publicationChannel;
  private boolean enabled;

  @Reference
  private ResourceResolverProvider resourceResolverProvider;

  @Activate
  @Modified
  private void activate(ApplicationResourceDataConfig config) {
    publicationChannel = config.publication_channel();
    enabled = config.enabled();
  }

  @Override
  public String getId() {
    return "WebSight-Application-Resources-Handler";
  }

  @Override
  public boolean canHandle(String resourcePath) {
    if (!enabled) {
      return false;
    }

    try (ResourceResolver resourceResolver = resourceResolverProvider.getResourceResolver()) {
      return Arrays.stream(resourceResolver.getSearchPath()).anyMatch(resourcePath::startsWith);
    } catch (LoginException e) {
      LOG.error("Cannot get resource resolver.");
    }
    return false;
  }

  @Override
  public PublishData<WebResourceModel> getPublishData(String resourcePath) {
    try (ResourceResolver resourceResolver = resourceResolverProvider.getResourceResolver()) {
      Resource resource = resourceResolver.getResource(resourcePath);
      if (resource != null) {
        WebResourceModel data = resolveData(resource);
        return new PublishData<>(resourcePath, publicationChannel, WebResourceModel.class, data);
      } else {
        LOG.warn("Cannot prepare publish data for {}. Resource doesn't exist.", resourcePath);
      }
    } catch (LoginException e) {
      LOG.error("Cannot get resource resolver.");
    } catch (PublishException e) {
      LOG.error("Cannot prepare publish data for {}.", resourcePath, e);
    }
    return null;
  }

  @Override
  public UnpublishData<WebResourceModel> getUnpublishData(String resourcePath) {
    return new UnpublishData<>(resourcePath, publicationChannel, WebResourceModel.class);
  }

  public WebResourceModel resolveData(Resource resource) throws PublishException {
    try (InputStream inputStream = resource.adaptTo(InputStream.class)) {
      if (inputStream != null) {
        return new WebResourceModel(ByteBuffer.wrap(inputStream.readAllBytes()));
      }
    } catch (IOException e) {
      LOG.warn("IOException occurred when storing data for resource {}", resource.getPath());
      throw new PublishException("StreamX publish failure for resource " + resource.getPath());
    }

    return null;
  }
}
