package com.app.model;

import com.app.constants.Constants;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = Constants.ROLE_TABLE_NAME)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Role extends BaseEntity {
  @Column(unique = true, nullable = false)
  private String name; // ROLE_USER, ROLE_ADMIN
}
