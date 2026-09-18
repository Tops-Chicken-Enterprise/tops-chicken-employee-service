package com.topschicken.employeeservice.entity;

import com.topschicken.employeeservice.entity.enums.VaccinationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "vaccination_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VaccinationRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vaccine_type_id", nullable = false)
    private VaccineType vaccineType;

    @Column(name = "dose_number", nullable = false)
    @Builder.Default
    private Integer doseNumber = 1;

    @Column(name = "administered_date", nullable = false)
    private LocalDate administeredDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "administered_by", length = 100)
    private String administeredBy;

    @Column(name = "certificate_url", length = 255)
    private String certificateUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private VaccinationStatus status = VaccinationStatus.VALID;
}