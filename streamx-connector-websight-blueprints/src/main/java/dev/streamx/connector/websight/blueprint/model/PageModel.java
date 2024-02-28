package dev.streamx.connector.websight.blueprint.model;

import java.nio.ByteBuffer;

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
