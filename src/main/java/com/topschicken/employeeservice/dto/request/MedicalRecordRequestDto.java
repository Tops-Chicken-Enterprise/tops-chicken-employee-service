package com.topschicken.employeeservice.dto.request;

import com.topschicken.employeeservice.entity.enums.FitnessStatus;
import com.topschicken.employeeservice.entity.enums.MedicalRecordType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordRequestDto {

    @NotNull(message = "Employee ID is mandatory")
    private Long employeeId;

    @NotNull(message = "Record type is mandatory")
    private MedicalRecordType recordType;

    @NotNull(message = "Checkup date is mandatory")
    private LocalDate checkupDate;

    private LocalDate expiryDate;

    @NotBlank(message = "Clinic or Hospital name is mandatory")
    @Size(max = 150, message = "Clinic/Hospital name must not exceed 150 characters")
    private String clinicOrHospital;

    @Size(max = 100, message = "Doctor name must not exceed 100 characters")
    private String doctorName;

    @NotNull(message = "Fitness status is mandatory")
    private FitnessStatus fitnessStatus;

    private String remarks;

    @Size(max = 255, message = "Report URL must not exceed 255 characters")
    private String reportUrl;

    private Long verifiedById;
}