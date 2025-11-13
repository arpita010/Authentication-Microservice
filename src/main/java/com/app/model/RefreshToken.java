package com.app.model;

import com.app.constants.Constants;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = Constants.REFRESH_TOKEN_TABLE_NAME,
    indexes = {
      @Index(name = "idx_rt_jti", columnList = "jti", unique = true),
      @Index(name = "idx_rt_user", columnList = "user_id")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {

  @Column(nullable = false, unique = true, updatable = false, length = 64)
  private String jti;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  private User user;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private boolean revoked;

  // jti of next token when rotated
  private String replacedByToken;
}
