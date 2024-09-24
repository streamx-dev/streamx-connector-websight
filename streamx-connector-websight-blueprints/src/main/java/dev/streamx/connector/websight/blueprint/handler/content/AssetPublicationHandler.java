package dev.streamx.connector.websight.blueprint.handler.content;

import dev.streamx.connector.websight.blueprint.ResourceResolverProvider;
import dev.streamx.blueprints.data.Asset;
import dev.streamx.sling.connector.PublicationHandler;
import dev.streamx.sling.connector.PublishData;
import dev.streamx.sling.connector.UnpublishData;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import org.apache.http.client.utils.URLEncodedUtils;
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
import pl.ds.websight.assets.core.api.Rendition;
import pl.ds.websight.publishing.framework.PublishException;

@Component
@Designate(ocd = AssetPublicationHandlerConfig.class)
public class AssetPublicationHandler implements PublicationHandler<Asset> {

  private static final Logger LOG = LoggerFactory.getLogger(AssetPublicationHandler.class);
  private static final String ASSETS_PATH_REGEXP = "^/published/[^/]+/assets/.*$";

  @Reference
  private ResourceResolverProvider resourceResolverProvider;

  private String publicationChannel;
  private boolean enabled;

  @Activate
  @Modified
  private void activate(AssetPublicationHandlerConfig config) {
    publicationChannel = config.publication_channel();
    enabled = config.enabled();
  }

  @Override
  public String getId() {
    return "WebSight-Asset-Handler";
  }

  @Override
  public boolean canHandle(String resourcePath) {
    return enabled && resourcePath.matches(ASSETS_PATH_REGEXP);
  }

  @Override
  public PublishData<Asset> getPublishData(String resourcePath) {
    try (ResourceResolver resourceResolver = resourceResolverProvider.getResourceResolver()) {
      Resource resource = resourceResolver.getResource(resourcePath);
      if (resource != null) {
        Asset asset = resolveData(resource);
        return new PublishData<>(getStoragePath(resourcePath), publicationChannel, Asset.class,
            asset);
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
  public UnpublishData<Asset> getUnpublishData(String resourcePath) {
    return new UnpublishData<>(getStoragePath(resourcePath), publicationChannel, Asset.class);
  }

  private String getStoragePath(String resourcePath) {
    String extension = getAssetExtension(resourcePath);
    return resourcePath + "/jcr:content/renditions/original" + extension;
  }

  private Asset resolveData(Resource publishedResource) throws PublishException {
    try (InputStream dataStream = getStorageData(publishedResource)) {
      return new Asset(ByteBuffer.wrap(dataStream.readAllBytes()));
    } catch (IOException e) {
      throw new PublishException(
          String.format("Cannot read resource %s data", publishedResource.getPath()), e);
    }
  }

  private InputStream getStorageData(Resource resource) {
    return getOriginalRendition(resource).map(Rendition::openStream).orElseThrow(
        () -> new IllegalStateException(
            "Cannot get original rendition data for " + resource.getPath()));
  }

  private Optional<Rendition> getOriginalRendition(Resource resource) {
    return Optional.ofNullable(resource.adaptTo(pl.ds.websight.assets.core.api.Asset.class)).map(
        pl.ds.websight.assets.core.api.Asset::getOriginalRendition);
  }

  private String getAssetExtension(String resourcePath) {
    int dotIndex = resourcePath.lastIndexOf(".");
    if (dotIndex != -1) {
      int slashIndex = resourcePath.lastIndexOf("/");
      if (dotIndex > slashIndex) {
        return resourcePath.substring(dotIndex);
      }
    }
    return "";
  }
}
