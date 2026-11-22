package com.topschicken.employeeservice.api;

import com.topschicken.employeeservice.dto.request.EmployeeBankAccountRequestDto;
import com.topschicken.employeeservice.dto.response.ApiResponseDto;
import com.topschicken.employeeservice.dto.response.EmployeeBankAccountResponseDto;
import com.topschicken.employeeservice.service.EmployeeBankAccountService;
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
@RequestMapping("/employee/{employeeId}/bank-accounts")
@RequiredArgsConstructor
public class EmployeeBankAccountController {

    private final EmployeeBankAccountService bankAccountService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<EmployeeBankAccountResponseDto>> createBankAccount(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @Valid @RequestBody EmployeeBankAccountRequestDto requestDto) {
        log.info("REST request to create bank account for employee ID: {}", employeeId);
        EmployeeBankAccountResponseDto response = bankAccountService.createBankAccount(employeeId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(),
                        "Employee bank account added successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<EmployeeBankAccountResponseDto>>> getBankAccountsByEmployee(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId) {
        List<EmployeeBankAccountResponseDto> response = bankAccountService.getBankAccountsByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Employee bank accounts retrieved successfully", response));
    }

    @GetMapping("/{bankAccountId}")
    public ResponseEntity<ApiResponseDto<EmployeeBankAccountResponseDto>> getBankAccountById(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @PathVariable @Positive(message = "Bank account ID must be positive") Long bankAccountId) {
        EmployeeBankAccountResponseDto response = bankAccountService.getBankAccountById(employeeId, bankAccountId);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Employee bank account retrieved successfully", response));
    }

    @PutMapping("/{bankAccountId}")
    public ResponseEntity<ApiResponseDto<EmployeeBankAccountResponseDto>> updateBankAccount(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @PathVariable @Positive(message = "Bank account ID must be positive") Long bankAccountId,
            @Valid @RequestBody EmployeeBankAccountRequestDto requestDto) {
        log.info("REST request to update bank account ID: {} for employee ID: {}", bankAccountId, employeeId);
        EmployeeBankAccountResponseDto response = bankAccountService.updateBankAccount(
                employeeId, bankAccountId, requestDto
        );
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Employee bank account updated successfully", response));
    }

    @DeleteMapping("/{bankAccountId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteBankAccount(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @PathVariable @Positive(message = "Bank account ID must be positive") Long bankAccountId) {
        log.info("REST request to delete bank account ID: {} for employee ID: {}", bankAccountId, employeeId);
        bankAccountService.deleteBankAccount(employeeId, bankAccountId);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.NO_CONTENT.value(),
                "Employee bank account deleted successfully", null));
    }
}