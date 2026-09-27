package com.topschicken.employeeservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeBankAccountRequestDto {

    @NotBlank(message = "Bank name is mandatory")
    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    @NotBlank(message = "Branch name is mandatory")
    @Size(max = 100, message = "Branch name must not exceed 100 characters")
    private String branchName;

    @NotBlank(message = "Account number is mandatory")
    @Size(max = 50, message = "Account number must not exceed 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9-]+$", message = "Account number can only contain letters, numbers, and hyphens")
    private String accountNumber;

    @NotBlank(message = "Account holder name is mandatory")
    @Size(max = 100, message = "Account holder name must not exceed 100 characters")
    private String accountHolderName;

    @NotNull(message = "Primary account flag is mandatory")
    private Boolean isPrimary;
}