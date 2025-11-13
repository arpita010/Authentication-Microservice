package com.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "role")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role extends BaseEntity {
  @Id private UUID id = UUID.randomUUID();

  @Column(unique = true, nullable = false)
  private String name; // ROLE_USER, ROLE_ADMIN
}
