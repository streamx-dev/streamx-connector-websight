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
 * Unclosed RR is created <a href="https://github.com/apache/sling-org-apache-sling-scripting-core/commit/fa01f5d8740af82008bf2048cfea64d838a0a43a#diff-51cd8dd3f10aa864fb79e205cde1814ee5233e60b08c441c33aa9a597ba8691dR74">here</a>.
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
