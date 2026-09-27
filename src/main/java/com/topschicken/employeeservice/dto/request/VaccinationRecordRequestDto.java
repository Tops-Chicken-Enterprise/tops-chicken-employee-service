package com.topschicken.employeeservice.dto.request;

import com.topschicken.employeeservice.entity.enums.VaccinationStatus;
import jakarta.validation.constraints.Min;
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
public class VaccinationRecordRequestDto {

    @NotNull(message = "Employee ID is mandatory")
    private Long employeeId;

    @NotNull(message = "Vaccine Type ID is mandatory")
    private Long vaccineTypeId;

    @NotNull(message = "Dose number is mandatory")
    @Min(value = 1, message = "Dose number must be at least 1")
    private Integer doseNumber;

    @NotNull(message = "Administered date is mandatory")
    private LocalDate administeredDate;

    private LocalDate expiryDate;

    @Size(max = 50, message = "Batch number must not exceed 50 characters")
    private String batchNumber;

    @Size(max = 100, message = "Administered by must not exceed 100 characters")
    private String administeredBy;

    @Size(max = 255, message = "Certificate URL must not exceed 255 characters")
    private String certificateUrl;

    private VaccinationStatus status;
}