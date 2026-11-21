package com.topschicken.employeeservice.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeTransferRequestDto {

    @NotNull(message = "New designation ID is mandatory")
    @Positive(message = "New designation ID must be a positive number")
    private Long newDesignationId;

    private Long newFarmId;

    private Long newShedId;

    private Long newSupervisorId;
}