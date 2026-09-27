package com.topschicken.employeeservice.dto.response;

import com.topschicken.employeeservice.entity.enums.VaccinationStatus;
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
public class VaccinationRecordResponseDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String empCode;
    private Long vaccineTypeId;
    private String vaccineTypeCode;
    private String vaccineTypeName;
    private Integer doseNumber;
    private LocalDate administeredDate;
    private LocalDate expiryDate;
    private String batchNumber;
    private String administeredBy;
    private String certificateUrl;
    private VaccinationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}