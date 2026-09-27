package com.topschicken.employeeservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VaccineTypeRequestDto {

    @NotBlank(message = "Vaccine code is mandatory")
    @Size(max = 30, message = "Code must not exceed 30 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Code must contain only uppercase letters, numbers, hyphens or underscores")
    private String code;

    @NotBlank(message = "Vaccine name is mandatory")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotNull(message = "Validity in months is mandatory")
    @Positive(message = "Validity months must be greater than zero")
    private Integer validityMonths;

    @NotNull(message = "Mandatory flag is required")
    private Boolean isMandatory;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;
}