package dev.streamx.connector.websight.blueprint.handler.content;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(name = "StreamX Assets Publication Config")
@interface AssetDataHandlerConfig {

  @AttributeDefinition(name = "Publications channel name")
  String publication_channel();
}
