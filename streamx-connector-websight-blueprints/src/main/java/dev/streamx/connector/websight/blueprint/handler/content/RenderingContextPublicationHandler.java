package dev.streamx.connector.websight.blueprint.handler.content;

import dev.streamx.blueprints.data.RenderingContext;
import dev.streamx.blueprints.data.RenderingContext.OutputType;
import dev.streamx.connector.websight.blueprint.PageDataService;
import dev.streamx.connector.websight.blueprint.ResourceResolverProvider;
import dev.streamx.sling.connector.PublicationHandler;
import dev.streamx.sling.connector.PublishData;
import dev.streamx.sling.connector.UnpublishData;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.publishing.framework.PublishException;

@Component
@Designate(ocd = RenderingContextPublicationHandlerConfig.class)
public class RenderingContextPublicationHandler implements PublicationHandler<RenderingContext> {

  private static final Logger LOG =
      LoggerFactory.getLogger(RenderingContextPublicationHandler.class);

  @Reference
  private ResourceResolverProvider resourceResolverProvider;

  @Reference
  private PageDataService pageDataService;

  private String publicationChannel;
  private boolean enabled;

  @Activate
  @Modified
  private void activate(RenderingContextPublicationHandlerConfig config) {
    publicationChannel = config.publication_channel();
    enabled = config.enabled();
  }

  @Override
  public String getId() {
    return "WebSight-Rendering-Context-Handler";
  }

  @Override
  public boolean canHandle(String resourcePath) {
    return enabled && pageDataService.isPageTemplate(resourcePath);
  }

  @Override
  public PublishData<RenderingContext> getPublishData(String resourcePath) {
    try (ResourceResolver resourceResolver = resourceResolverProvider.getResourceResolver()) {
      Resource resource = resourceResolver.getResource(resourcePath);
      if (resource != null) {
        RenderingContext renderer = resolveData(resource);
        if (renderer != null) {
          return new PublishData<>(resourcePath, publicationChannel, RenderingContext.class,
              renderer);
        }
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
  public UnpublishData<RenderingContext> getUnpublishData(String resourcePath) {
    return new UnpublishData<>(resourcePath, publicationChannel,
        RenderingContext.class);
  }

  private RenderingContext resolveData(Resource resource) throws PublishException {
    ValueMap properties = Optional.ofNullable(resource.getChild("jcr:content"))
        .map(Resource::getValueMap)
        .orElse(ValueMap.EMPTY);
    String dataKeyMatchPattern = properties.get("dataKeyMatchPattern", String.class);
    String outputKeyTemplate = properties.get("outputKeyTemplate", String.class);
    if (StringUtils.isNoneBlank(dataKeyMatchPattern, outputKeyTemplate)) {
      // Use same key as is used for the Renderer published for the template page.
      String rendererKey = resource.getPath() + ".html";
      return new RenderingContext(rendererKey, dataKeyMatchPattern, outputKeyTemplate,
          OutputType.PAGE);
    }
    LOG.info("Cannot prepare publish data for {}. Resource doesn't contain required properties.",
        resource.getPath());
    return null;
  }

}
