package com.app.model;

import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Data
@MappedSuperclass
public class BaseEntity {
  @CreationTimestamp private Date createdAt;
  @UpdateTimestamp private Date updatedAt;
}
