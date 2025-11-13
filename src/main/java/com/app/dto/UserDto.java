package com.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
  private String name;
  private String email;
  private Boolean enable;
  private String image;
  private Instant createdAt;
  private Instant updatedAt;
}
