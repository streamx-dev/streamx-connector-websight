package dev.streamx.connector.websight.blueprint.handler.application;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "StreamX Blueprints - Application Resources Publication Config")
@interface ApplicationResourceDataConfig {

  @AttributeDefinition(name = "Publications channel name")
  String publication_channel() default "web-resources";

  @AttributeDefinition(name = "Enable handler", description =
      "If the flag is unset the handler won't proceed.")
  boolean enabled() default true;
}
