package dev.streamx.connector.websight.blueprint.handler.application;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "StreamX Application Resources Publication Config")
@interface ApplicationResourceDataConfig {

  @AttributeDefinition(name = "Publications channel name")
  String publication_channel();
}
