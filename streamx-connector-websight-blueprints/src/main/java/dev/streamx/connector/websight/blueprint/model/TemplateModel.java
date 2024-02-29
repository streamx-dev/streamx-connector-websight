package dev.streamx.connector.websight.blueprint.model;

import java.nio.ByteBuffer;

public class TemplateModel {
  private ByteBuffer content;

  public TemplateModel() {
  }

  public TemplateModel(ByteBuffer content) {
    this.content = content;
  }

  public ByteBuffer getContent() {
    return content;
  }

  public void setContent(ByteBuffer content) {
    this.content = content;
  }
}
