package dev.streamx.connector.websight.blueprint.handler.content;

import static pl.ds.websight.publishing.framework.PublishService.CONTENT_ROOT;
import static pl.ds.websight.publishing.framework.PublishService.PUBLISHED_ROOT;

import org.apache.commons.lang3.StringUtils;

public class PathUtil {

  private PathUtil() {
    // No instances
  }

  public static String getPublishedPath(String path) {
    if (isContentPath(path)) {
      return PUBLISHED_ROOT + StringUtils.removeStart(path, CONTENT_ROOT);
    }
    throw new IllegalStateException(
        String.format("The path %s is not starting with parent path %s", path, CONTENT_ROOT));
  }

  private static boolean isContentPath(String path) {
    return path.startsWith(CONTENT_ROOT + "/") || path.equals(CONTENT_ROOT);
  }
}