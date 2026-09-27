package com.topschicken.employeeservice.dto.request;

import com.topschicken.employeeservice.entity.enums.SanitizationStatus;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BiosecurityEntryLogRequestDto {

    @NotNull(message = "Employee ID is mandatory")
    private Long employeeId;

    @NotNull(message = "Farm ID is mandatory")
    private Long farmId;

    @NotNull(message = "Shed ID is mandatory")
    private Long shedId;

    @NotNull(message = "Entry time is mandatory")
    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    @NotNull(message = "Shower completed flag is required")
    private Boolean showerCompleted;

    @NotNull(message = "Clothing changed flag is required")
    private Boolean clothingChanged;

    @DecimalMin(value = "30.0", message = "Body temperature must be realistic")
    @DecimalMax(value = "45.0", message = "Body temperature must be realistic")
    private BigDecimal bodyTemperature;

    @NotNull(message = "Sanitization status is mandatory")
    private SanitizationStatus sanitizationStatus;


    private Long clearedById;

    @Size(max = 255, message = "Remarks must not exceed 255 characters")
    private String remarks;
}