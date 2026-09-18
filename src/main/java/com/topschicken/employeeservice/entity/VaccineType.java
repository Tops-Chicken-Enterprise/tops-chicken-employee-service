package com.topschicken.employeeservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vaccine_type")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccineType extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "validity_months", nullable = false)
    private Integer validityMonths;

    @Column(name = "is_mandatory", nullable = false)
    @Builder.Default
    private Boolean isMandatory = true;

    @Column(name = "description", length = 255)
    private String description;

    @OneToMany(mappedBy = "vaccineType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VaccinationRecord> vaccinationRecords = new ArrayList<>();
}