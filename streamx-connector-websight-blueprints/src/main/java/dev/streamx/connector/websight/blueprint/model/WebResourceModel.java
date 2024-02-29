package dev.streamx.connector.websight.blueprint.model;

import java.nio.ByteBuffer;

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
