package dev.streamx.connector.websight.blueprint.reference.relay.model;

import java.nio.ByteBuffer;
import org.apache.avro.specific.AvroGenerated;

@AvroGenerated
public class WebResourceModel {
  private ByteBuffer content;

  public WebResourceModel() {
  }

  public WebResourceModel(ByteBuffer content) {
    this.content = content;
  }

  public ByteBuffer getContent() {
    return content;
  }

  public void setContent(ByteBuffer content) {
    this.content = content;
  }
}
