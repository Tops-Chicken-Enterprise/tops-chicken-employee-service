package com.topschicken.employeeservice.dto.request;

import com.topschicken.employeeservice.entity.enums.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAddressRequestDto {

    @NotNull(message = "Address type is mandatory")
    private AddressType addressType;

    @NotBlank(message = "Address line 1 is mandatory")
    @Size(max = 150, message = "Line 1 must not exceed 150 characters")
    private String line1;

    @Size(max = 150, message = "Line 2 must not exceed 150 characters")
    private String line2;

    @Size(max = 100, message = "Street must not exceed 100 characters")
    private String street;

    @NotBlank(message = "City is mandatory")
    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    private String postalCode;
}