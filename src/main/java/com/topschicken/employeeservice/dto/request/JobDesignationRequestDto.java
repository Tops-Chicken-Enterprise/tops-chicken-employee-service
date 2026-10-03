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
public class JobDesignationRequestDto {

    @NotNull(message = "Department ID is mandatory")
    @Positive(message = "Department ID must be a valid positive number")
    private Long departmentId;

    @NotBlank(message = "Designation code is mandatory")
    @Size(max = 30, message = "Code must not exceed 30 characters")
    @Pattern(regexp = "^[A-Z0-9_-]+$", message = "Code must contain only uppercase letters, numbers, hyphens or underscores")
    private String code;

    @NotBlank(message = "Designation title is mandatory")
    @Size(min = 2, max = 100, message = "Title must be between 2 and 100 characters")
    private String title;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private Boolean isActive;
}