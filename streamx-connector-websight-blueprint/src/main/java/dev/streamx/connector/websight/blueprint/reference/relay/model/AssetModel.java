package dev.streamx.connector.websight.blueprint.reference.relay.model;

import java.nio.ByteBuffer;
import org.apache.avro.specific.AvroGenerated;

@AvroGenerated
public class AssetModel {
  private ByteBuffer content;

  public AssetModel() {
  }

  public AssetModel(ByteBuffer content) {
    this.content = content;
  }

  public ByteBuffer getContent() {
    return content;
  }

  public void setContent(ByteBuffer content) {
    this.content = content;
  }
}
