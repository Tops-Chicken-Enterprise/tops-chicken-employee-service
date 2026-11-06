package com.topschicken.employeeservice.api;

import com.topschicken.employeeservice.dto.request.EmployeeRequestDto;
import com.topschicken.employeeservice.dto.response.ApiResponseDto;
import com.topschicken.employeeservice.dto.response.EmployeeResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.entity.enums.EmploymentStatus;
import com.topschicken.employeeservice.service.EmployeeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Validated
@RestController
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<EmployeeResponseDto>> createEmployee(
            @Valid @RequestBody EmployeeRequestDto requestDto) {
        log.info("REST request to create employee: {}", requestDto.getEmpCode());
        EmployeeResponseDto response = employeeService.createEmployee(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(),
                        "Employee created successfully in DB and WSO2 IS", response)
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<EmployeeResponseDto>> getEmployeeById(
            @PathVariable @Positive(message = "Employee ID must be positive") Long id) {
        EmployeeResponseDto response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Employee retrieved successfully", response)
        );
    }

    @GetMapping("/code/{empCode}")
    public ResponseEntity<ApiResponseDto<EmployeeResponseDto>> getEmployeeByCode(
            @PathVariable String empCode) {
        EmployeeResponseDto response = employeeService.getEmployeeByEmpCode(empCode);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Employee retrieved successfully", response)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<EmployeeResponseDto>> updateEmployee(
            @PathVariable @Positive(message = "Employee ID must be positive") Long id,
            @Valid @RequestBody EmployeeRequestDto requestDto) {
        log.info("REST request to update employee ID: {}", id);
        EmployeeResponseDto response = employeeService.updateEmployee(id, requestDto);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Employee updated successfully", response)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PagedResponseDto<EmployeeResponseDto>>> searchEmployees(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) EmploymentStatus status,
            @RequestParam(required = false) Long farmId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        log.info("REST request to search employees");
        PagedResponseDto<EmployeeResponseDto> response = employeeService.searchEmployees(
                search, departmentId, status, farmId, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Employees retrieved successfully", response)
        );
    }
}