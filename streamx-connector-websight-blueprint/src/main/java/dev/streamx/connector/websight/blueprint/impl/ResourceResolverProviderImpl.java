package dev.streamx.connector.websight.blueprint.impl;

import static org.apache.sling.api.resource.ResourceResolverFactory.SUBSERVICE;

import dev.streamx.connector.websight.blueprint.ResourceResolverProvider;
import java.util.Map;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class ResourceResolverProviderImpl implements ResourceResolverProvider {

  private static final String SERVICE_USER_ID = "streamx-connector-websight-blueprint";

  @Reference
  private ResourceResolverFactory resourceResolverFactory;

  @Override
  public ResourceResolver getResourceResolver() throws LoginException {
    return resourceResolverFactory.getServiceResourceResolver(Map.of(SUBSERVICE, SERVICE_USER_ID));
  }
}
