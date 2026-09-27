package com.topschicken.employeeservice.dto.response;

import com.topschicken.employeeservice.entity.enums.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeAddressResponseDto {
    private Long id;
    private AddressType addressType;
    private String line1;
    private String line2;
    private String street;
    private String city;
    private String postalCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}