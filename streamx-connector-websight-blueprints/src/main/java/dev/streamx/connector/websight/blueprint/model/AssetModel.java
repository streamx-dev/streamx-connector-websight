package dev.streamx.connector.websight.blueprint.model;

import java.nio.ByteBuffer;

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
