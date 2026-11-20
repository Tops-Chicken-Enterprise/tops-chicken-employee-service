package com.topschicken.employeeservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequestDto {

    @NotBlank(message = "Scope code is mandatory")
    @Pattern(regexp = "^[a-z0-9_:]+$", message = "Scope code must contain only lowercase letters, numbers, underscores, and colons (e.g., employee:read)")
    private String scopeCode;

    @NotBlank(message = "Display name is mandatory")
    private String displayName;

    private String description;
}