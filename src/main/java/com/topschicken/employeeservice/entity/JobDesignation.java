package com.topschicken.employeeservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "job_designation", indexes = {
        @Index(name = "idx_job_designation_code", columnList = "code"),
        @Index(name = "idx_job_designation_dept_id", columnList = "department_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDesignation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "wso2_group_id", length = 100)
    private String wso2GroupId;

    @Column(name = "wso2_role_id", length = 100)
    private String wso2RoleId;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "job_designation_permission",
            joinColumns = @JoinColumn(name = "job_designation_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private List<Permission> permissions;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}