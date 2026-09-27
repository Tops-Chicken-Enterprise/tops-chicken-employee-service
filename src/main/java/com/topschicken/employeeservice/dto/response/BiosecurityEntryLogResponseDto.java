package com.topschicken.employeeservice.dto.response;

import com.topschicken.employeeservice.entity.enums.SanitizationStatus;
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
public class BiosecurityEntryLogResponseDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String empCode;
    private Long farmId;
    private Long shedId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Boolean showerCompleted;
    private Boolean clothingChanged;
    private BigDecimal bodyTemperature;
    private SanitizationStatus sanitizationStatus;
    private Long clearedById;
    private String clearedByName;
    private String remarks;
    private LocalDateTime createdAt;
}