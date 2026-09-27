package com.topschicken.employeeservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccineTypeResponseDto {
    private Long id;
    private String code;
    private String name;
    private Integer validityMonths;
    private Boolean isMandatory;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}