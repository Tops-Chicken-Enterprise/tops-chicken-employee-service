package com.topschicken.employeeservice.dto.response;

import com.topschicken.employeeservice.entity.enums.FitnessStatus;
import com.topschicken.employeeservice.entity.enums.MedicalRecordType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponseDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String empCode;
    private MedicalRecordType recordType;
    private LocalDate checkupDate;
    private LocalDate expiryDate;
    private String clinicOrHospital;
    private String doctorName;
    private FitnessStatus fitnessStatus;
    private String remarks;
    private String reportUrl;
    private Long verifiedById;
    private String verifiedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}