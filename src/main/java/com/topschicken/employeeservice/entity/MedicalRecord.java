package com.topschicken.employeeservice.entity;

import com.topschicken.employeeservice.entity.enums.FitnessStatus;
import com.topschicken.employeeservice.entity.enums.MedicalRecordType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "medical_record")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "record_type", nullable = false, length = 30)
    private MedicalRecordType recordType;

    @Column(name = "checkup_date", nullable = false)
    private LocalDate checkupDate;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "clinic_or_hospital", nullable = false, length = 150)
    private String clinicOrHospital;

    @Column(name = "doctor_name", length = 100)
    private String doctorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "fitness_status", nullable = false, length = 30)
    private FitnessStatus fitnessStatus;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "report_url", length = 255)
    private String reportUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by")
    private Employee verifiedBy;
}