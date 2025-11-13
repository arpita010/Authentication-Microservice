package com.app.constants;

public enum Provider {
  LOCAL("LOCAL"),
  GOOGLE("GOOGLE"),
  GITHUB("GITHUB"),
  FACEBOOK("FACEBOOK");

  private String provider;

  Provider(String provider) {
    this.provider = provider;
  }
}
