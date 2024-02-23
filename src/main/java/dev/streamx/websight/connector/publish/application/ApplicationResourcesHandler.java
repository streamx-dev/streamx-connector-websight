package dev.streamx.websight.connector.publish.application;

import dev.streamx.sling.connector.StreamxPublicationService;
import java.util.List;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import pl.ds.websight.apps.activator.ApplicationResourceActivator;

@Component(property = {Constants.SERVICE_RANKING + ":Integer=" + Integer.MAX_VALUE})
public class ApplicationResourcesHandler implements ApplicationResourceActivator {

  @Reference
  private StreamxPublicationService streamxPublicationService;

  @Override
  public boolean activate(Resource resource) {
    streamxPublicationService.publish(List.of(resource.getPath()));
    return true;
  }
}
