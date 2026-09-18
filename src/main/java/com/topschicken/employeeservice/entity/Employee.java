package com.topschicken.employeeservice.entity;

import com.topschicken.employeeservice.entity.enums.EmploymentStatus;
import com.topschicken.employeeservice.entity.enums.EmploymentType;
import com.topschicken.employeeservice.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emp_code", nullable = false, unique = true, length = 20)
    private String empCode;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "middle_name", length = 50)
    private String middleName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "nic", nullable = false, unique = true, length = 20)
    private String nic;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 10)
    private Gender gender;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "contact_number", nullable = false, length = 20)
    private String contactNumber;

    @Column(name = "emergency_contact", nullable = false, length = 20)
    private String emergencyContact;

    @Column(name = "epf_number", unique = true, length = 30)
    private String epfNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 20)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, length = 20)
    @Builder.Default
    private EmploymentStatus employmentStatus = EmploymentStatus.PROBATION;

    @Column(name = "joined_date", nullable = false)
    private LocalDate joinedDate;

    @Column(name = "resigned_date")
    private LocalDate resignedDate;

    @Column(name = "biometric_id")
    private Integer biometricId;

    @Column(name = "farm_id")
    private Long farmId;

    @Column(name = "shed_id")
    private Long shedId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "designation_id", nullable = false)
    private JobDesignation designation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisor_id")
    private Employee supervisor;

    @Column(name = "wso2_user_id", unique = true, length = 100)
    private String wso2UserId;

    @Column(name = "profile_image_url", length = 255)
    private String profileImageUrl;

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmployeeAddress> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmployeeBankAccount> bankAccounts = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<EmployeeDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<MedicalRecord> medicalRecords = new ArrayList<>();

    @OneToMany(mappedBy = "employee", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<VaccinationRecord> vaccinationRecords = new ArrayList<>();
}