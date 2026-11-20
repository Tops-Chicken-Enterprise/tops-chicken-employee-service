package com.topschicken.employeeservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permission")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scope_code", nullable = false, unique = true, length = 100)
    private String scopeCode; // e.g., "employee:read"

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName; // e.g., "Read Employee Details"

    @Column(name = "description", length = 255)
    private String description;
}