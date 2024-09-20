package dev.streamx.connector.websight.blueprint.handler.content;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "StreamX Blueprints - Renderers Publication Config")
@interface RendererPublicationHandlerConfig {

  @AttributeDefinition(name = "Publications channel name")
  String publication_channel() default "renderers";

  @AttributeDefinition(name = "Enable handler", description =
      "If the flag is unset the handler won't proceed.")
  boolean enabled() default true;
}
