package com.topschicken.employeeservice.api;

import com.topschicken.employeeservice.dto.request.EmployeeAddressRequestDto;
import com.topschicken.employeeservice.dto.response.ApiResponseDto;
import com.topschicken.employeeservice.dto.response.EmployeeAddressResponseDto;
import com.topschicken.employeeservice.service.EmployeeAddressService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/employee/{employeeId}/addresses")
@RequiredArgsConstructor
public class EmployeeAddressController {

    private final EmployeeAddressService addressService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<EmployeeAddressResponseDto>> createAddress(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @Valid @RequestBody EmployeeAddressRequestDto requestDto) {
        log.info("REST request to create {} address for employee ID: {}", requestDto.getAddressType(), employeeId);
        EmployeeAddressResponseDto response = addressService.createAddress(employeeId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(),
                        "Employee address added successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<EmployeeAddressResponseDto>>> getAddressesByEmployee(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId) {
        List<EmployeeAddressResponseDto> response = addressService.getAddressesByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Employee addresses retrieved successfully", response));
    }

    @GetMapping("/{addressId}")
    public ResponseEntity<ApiResponseDto<EmployeeAddressResponseDto>> getAddressById(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @PathVariable @Positive(message = "Address ID must be positive") Long addressId) {
        EmployeeAddressResponseDto response = addressService.getAddressById(employeeId, addressId);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Employee address retrieved successfully", response));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponseDto<EmployeeAddressResponseDto>> updateAddress(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @PathVariable @Positive(message = "Address ID must be positive") Long addressId,
            @Valid @RequestBody EmployeeAddressRequestDto requestDto) {
        log.info("REST request to update address ID: {} for employee ID: {}", addressId, employeeId);
        EmployeeAddressResponseDto response = addressService.updateAddress(employeeId, addressId, requestDto);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Employee address updated successfully", response));
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteAddress(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @PathVariable @Positive(message = "Address ID must be positive") Long addressId) {
        log.info("REST request to delete address ID: {} for employee ID: {}", addressId, employeeId);
        addressService.deleteAddress(employeeId, addressId);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.NO_CONTENT.value(),
                "Employee address deleted successfully", null));
    }
}