package dev.streamx.connector.websight.blueprint.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;
import java.util.Arrays;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Marker;

/**
 * The Filter class is responsible for cleaning up log messages related to a known internal Sling issue
 * involving the creation of an unclosed resource resolver.
 */
@Component(service = TurboFilter.class)
public class UnclosedResourceResolverLogFilter extends TurboFilter {

  private static final String UNCLOSED_RR_LOGGER_NAME = "org.apache.sling.resourceresolver.impl.CommonResourceResolverFactoryImpl";
  private static final String UNCLOSED_RR_LOG_MESSAGE = "Unclosed ResourceResolver was created here: ";
  private static final String UNCLOSED_RR_CREATOR = "org.apache.sling.scripting.sightly.impl.engine.compiled.SlingHTLMasterCompiler";

  @Override
  public FilterReply decide(Marker marker, Logger logger, Level level, String message,
      Object[] objects,
      Throwable throwable) {
    if (isUnclosedResourceResolverMessage(logger, message) && isOpenedByKnownClass(throwable)) {
      return FilterReply.DENY;
    }
    return FilterReply.NEUTRAL;
  }

  private boolean isUnclosedResourceResolverMessage(Logger logger, String message) {
    return UNCLOSED_RR_LOGGER_NAME.equals(logger.getName()) && UNCLOSED_RR_LOG_MESSAGE.equals(
        message);
  }

  private boolean isOpenedByKnownClass(Throwable throwable) {
    return Arrays.stream(throwable.getStackTrace())
        .anyMatch(element -> element.getClassName().equals(UNCLOSED_RR_CREATOR));
  }

}
