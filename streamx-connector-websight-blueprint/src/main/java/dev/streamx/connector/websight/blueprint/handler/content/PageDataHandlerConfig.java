package dev.streamx.connector.websight.blueprint.handler.content;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "StreamX Page Publication Config")
@interface PageDataHandlerConfig {

  @AttributeDefinition(name = "Shorten content paths", description =
      "Shorten paths in content to not start with '/published/<space name>/pages' or '/published/<space name>'. "
          + "Shortening is unaware of the context, therefore replacing all string occurrences.")
  boolean shorten_content_paths() default true;

  @AttributeDefinition(name = "Add nofollow to external links")
  boolean nofollow_external_links() default false;

  @AttributeDefinition(name = "Skip adding nofollow to external links for hosts")
  String[] nofollow_external_links_disallowed_hosts() default {};

  @AttributeDefinition(name = "Publications channel name for pages")
  String pages_publication_channel();

  @AttributeDefinition(name = "Publications channel name for page templates")
  String templates_publication_channel();

  @AttributeDefinition(name = "Regex pattern to detect pages path which should be published as templates")
  String templates_pattern() default "^/[^/]+/[^/]+/pages/templates+/.*";
}
