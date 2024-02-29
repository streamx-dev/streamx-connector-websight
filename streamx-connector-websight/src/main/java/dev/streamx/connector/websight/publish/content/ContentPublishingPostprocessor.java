package dev.streamx.connector.websight.publish.content;

import dev.streamx.sling.connector.StreamxPublicationException;
import dev.streamx.sling.connector.StreamxPublicationService;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.publishing.framework.PublishException;
import pl.ds.websight.publishing.framework.spi.PublishingPostprocessor;
import pl.ds.websight.publishing.framework.spi.ResourceData;

@Component(service = PublishingPostprocessor.class)
public class ContentPublishingPostprocessor implements PublishingPostprocessor {

  private static final Logger LOG = LoggerFactory.getLogger(ContentPublishingPostprocessor.class);

  @Reference
  private StreamxPublicationService streamxPublicationService;

  @Override
  public void afterPublish(@NotNull List<ResourceData> resourcesData) throws PublishException {
    List<String> publishedResources = resourcesData.stream().map(ResourceData::getPublishedPath)
        .toList();
    try {
      streamxPublicationService.publish(publishedResources);
    } catch (StreamxPublicationException e) {
      LOG.error("Couldn't publish resources.", e);
      throw new PublishException("Publishing resources to StreamX failed.");
    }
  }

  @Override
  public void afterUnpublish(@NotNull List<ResourceData> resourcesData) throws PublishException {
    List<String> unpublishedResources = resourcesData.stream().map(ResourceData::getPublishedPath)
        .toList();
    try {
      streamxPublicationService.unpublish(unpublishedResources);
    } catch (StreamxPublicationException e) {
      LOG.error("Couldn't unpublish resources.", e);
      throw new PublishException("Unpublishing resources form StreamX failed.");
    }
  }

  @Override
  public @NotNull String getName() {
    return "StreamX-Content-Publication";
  }
}
