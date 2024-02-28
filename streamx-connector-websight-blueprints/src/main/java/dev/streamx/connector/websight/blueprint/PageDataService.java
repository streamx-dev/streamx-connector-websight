package dev.streamx.connector.websight.blueprint;

import java.io.IOException;
import java.io.InputStream;
import org.apache.sling.api.resource.Resource;

public interface PageDataService {
  InputStream getStorageData(Resource resource) throws IOException;

  boolean isPage(String resourcePath);

  boolean isPageTemplate(String resourcePath);

}
