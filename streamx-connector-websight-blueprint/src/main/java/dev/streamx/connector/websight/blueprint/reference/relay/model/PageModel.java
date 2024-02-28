package dev.streamx.connector.websight.blueprint.reference.relay.model;

import java.nio.ByteBuffer;
import org.apache.avro.specific.AvroGenerated;

@AvroGenerated
public class PageModel {
  private ByteBuffer content;

  public PageModel() {
  }

  public PageModel(ByteBuffer content) {
    this.content = content;
  }

  public ByteBuffer getContent() {
    return content;
  }

  public void setContent(ByteBuffer content) {
    this.content = content;
  }
}
