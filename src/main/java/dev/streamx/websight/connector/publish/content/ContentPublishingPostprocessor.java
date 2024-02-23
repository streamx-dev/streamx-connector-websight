package dev.streamx.websight.connector.publish.content;

import dev.streamx.sling.connector.StreamxPublicationService;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import pl.ds.websight.publishing.framework.spi.PublishingPostprocessor;
import pl.ds.websight.publishing.framework.spi.ResourceData;

@Component(service = PublishingPostprocessor.class)
public class ContentPublishingPostprocessor implements PublishingPostprocessor {

  @Reference
  private StreamxPublicationService streamxPublicationService;

  @Override
  public void afterPublish(@NotNull List<ResourceData> resourcesData) {
    List<String> publishedResources = resourcesData.stream().map(ResourceData::getPublishedPath)
        .toList();
    streamxPublicationService.publish(publishedResources);
  }

  @Override
  public void afterUnpublish(@NotNull List<ResourceData> resourcesData) {
    List<String> unpublishedResources = resourcesData.stream().map(ResourceData::getPublishedPath)
        .toList();
    streamxPublicationService.unpublish(unpublishedResources);
  }

  @Override
  public @NotNull String getName() {
    return "StreamX-Content-Publication";
  }
}
