package dev.streamx.connector.websight.blueprint;

import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;

public interface ResourceResolverProvider {

  ResourceResolver getResourceResolver() throws LoginException;

}
