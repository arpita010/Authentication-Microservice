package com.app.constants;

public enum TokenType {
  ACCESS("ACCESS"),
  REFRESH("REFRESH");

  private String type;

  TokenType(String type) {
    this.type = type;
  }

  public String getType() {
    return this.type;
  }
}
