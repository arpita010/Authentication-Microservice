package com.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenResponse {
  private String accessToken;
  private String refreshToken;
  private Long expiresIn;
  private String tokenType;
  private UserDto user;

  public static TokenResponse bearer(String accessToken, String refreshToken, long expiresIn) {
    return new TokenResponse(accessToken, refreshToken, expiresIn, "Bearer", null);
  }

  public static TokenResponse bearerWithUser(
      String accessToken, String refreshToken, long expiresIn, UserDto user) {
    return new TokenResponse(accessToken, refreshToken, expiresIn, "Bearer", user);
  }
}
