package com.topschicken.employeeservice.api;

import com.topschicken.employeeservice.dto.request.DepartmentRequestDto;
import com.topschicken.employeeservice.dto.response.ApiResponseDto;
import com.topschicken.employeeservice.dto.response.DepartmentResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.service.DepartmentService;
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

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/department")
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<DepartmentResponseDto>> createDepartment(
            @Valid @RequestBody DepartmentRequestDto requestDto) {
        log.info("REST request to create department with code: {}", requestDto.getCode());
        DepartmentResponseDto response = departmentService.createDepartment(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponseDto.success(HttpStatus.CREATED.value(),
                                "Department created successfully", response)
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<DepartmentResponseDto>> getDepartmentById(
            @PathVariable @Positive(message = "Department ID must be a positive number") Long id) {
        log.info("REST request to get department by ID: {}", id);
        DepartmentResponseDto response = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(), "Department retrieved successfully", response)
        );
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponseDto<DepartmentResponseDto>> getDepartmentByCode(@PathVariable String code) {
        log.info("REST request to get department by code: {}", code);
        DepartmentResponseDto response = departmentService.getDepartmentByCode(code);
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(), "Department retrieved successfully", response)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<DepartmentResponseDto>> updateDepartment(
            @PathVariable @Positive(message = "Department ID must be a positive number") Long id,
            @Valid @RequestBody DepartmentRequestDto requestDto) {
        log.info("REST request to update department ID: {}", id);
        DepartmentResponseDto response = departmentService.updateDepartment(id, requestDto);
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(), "Department updated successfully", response)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponseDto<DepartmentResponseDto>> toggleDepartmentStatus(
            @PathVariable @Positive(message = "Department ID must be a positive number") Long id,
            @RequestParam boolean isActive) {
        log.info("REST request to toggle status for department ID: {} to isActive: {}", id, isActive);
        DepartmentResponseDto response = departmentService.toggleDepartmentStatus(id, isActive);
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(),
                        "Department status updated successfully", response)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PagedResponseDto<DepartmentResponseDto>>> getAllDepartments(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("REST request to get paginated departments with search: '{}', isActive: '{}'", search, isActive);
        PagedResponseDto<DepartmentResponseDto> response = departmentService.getAllDepartments(
                search, isActive, pageable
        );
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(), "Departments retrieved successfully", response)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponseDto<List<DepartmentResponseDto>>> getActiveDepartments() {
        log.info("REST request to get active departments for dropdown");
        List<DepartmentResponseDto> response = departmentService.getActiveDepartments();
        return ResponseEntity.ok(ApiResponseDto.success(
                HttpStatus.OK.value(), "Active departments retrieved successfully", response)
        );
    }
}