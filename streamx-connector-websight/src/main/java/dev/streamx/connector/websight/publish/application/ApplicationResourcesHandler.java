package dev.streamx.connector.websight.publish.application;

import dev.streamx.sling.connector.StreamxPublicationException;
import dev.streamx.sling.connector.StreamxPublicationService;
import java.util.List;
import org.apache.sling.api.resource.Resource;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.apps.activator.ApplicationResourceActivator;

@Component(property = {Constants.SERVICE_RANKING + ":Integer=" + Integer.MAX_VALUE})
public class ApplicationResourcesHandler implements ApplicationResourceActivator {

  private static final Logger LOG = LoggerFactory.getLogger(ApplicationResourcesHandler.class);

  @Reference
  private StreamxPublicationService streamxPublicationService;

  @Override
  public boolean activate(Resource resource) {
    try {
      streamxPublicationService.publish(List.of(resource.getPath()));
    } catch (StreamxPublicationException e) {
      LOG.error("Couldn't publish resources.", e);
      return false;
    }
    return true;
  }
}
