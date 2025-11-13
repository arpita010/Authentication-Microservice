package com.app.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterResponse {
  private UUID id;
  private String email;
  private String name;
  private String image;
  private Boolean enabled;
  private Instant createdAt;
  private Instant updatedAt;
}
