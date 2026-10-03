package com.topschicken.employeeservice.api;

import com.topschicken.employeeservice.dto.request.JobDesignationRequestDto;
import com.topschicken.employeeservice.dto.response.ApiResponseDto;
import com.topschicken.employeeservice.dto.response.JobDesignationResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.service.JobDesignationService;
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
@RequestMapping("/job-designation")
@RequiredArgsConstructor
public class JobDesignationController {

    private final JobDesignationService jobDesignationService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<JobDesignationResponseDto>> createDesignation(
            @Valid @RequestBody JobDesignationRequestDto requestDto) {
        log.info("REST request to create job designation: {}", requestDto.getCode());
        JobDesignationResponseDto response = jobDesignationService.createDesignation(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponseDto.success(HttpStatus.CREATED.value(),
                                "Job designation created successfully", response)
                );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<JobDesignationResponseDto>> getDesignationById(
            @PathVariable @Positive(message = "Job designation ID must be a positive number") Long id) {
        log.info("REST request to get job designation by ID: {}", id);
        JobDesignationResponseDto response = jobDesignationService.getDesignationById(id);
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(),
                        "Job designation retrieved successfully", response)
        );
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponseDto<JobDesignationResponseDto>> getDesignationByCode(@PathVariable String code) {
        log.info("REST request to get job designation by code: {}", code);
        JobDesignationResponseDto response = jobDesignationService.getDesignationByCode(code);
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(),
                        "Job designation retrieved successfully", response)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<JobDesignationResponseDto>> updateDesignation(
            @PathVariable @Positive(message = "Job designation ID must be a positive number") Long id,
            @Valid @RequestBody JobDesignationRequestDto requestDto) {
        log.info("REST request to update job designation ID: {}", id);
        JobDesignationResponseDto response = jobDesignationService.updateDesignation(id, requestDto);
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(),
                        "Job designation updated successfully", response)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponseDto<JobDesignationResponseDto>> toggleDesignationStatus(
            @PathVariable @Positive(message = "Job designation ID must be a positive number") Long id,
            @RequestParam boolean isActive) {
        log.info("REST request to toggle status for job designation ID: {} to isActive: {}", id, isActive);
        JobDesignationResponseDto response = jobDesignationService.toggleDesignationStatus(id, isActive);
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(),
                        "Job designation status updated successfully", response)
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PagedResponseDto<JobDesignationResponseDto>>> getAllDesignations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean isActive,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info(
                "REST request to get paginated job designations. Search: '{}', deptId: {}, isActive: {}",
                search, departmentId, isActive
        );
        PagedResponseDto<JobDesignationResponseDto> response = jobDesignationService.getAllDesignations(
                search, departmentId, isActive, pageable
        );
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(),
                        "Job designations retrieved successfully", response)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponseDto<List<JobDesignationResponseDto>>> getActiveDesignations() {
        log.info("REST request to get active job designations");
        List<JobDesignationResponseDto> response = jobDesignationService.getActiveDesignations();
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(),
                        "Active job designations retrieved successfully", response)
        );
    }

    @GetMapping("/department/{departmentId}/active")
    public ResponseEntity<ApiResponseDto<List<JobDesignationResponseDto>>> getActiveDesignationsByDepartment(
            @PathVariable @Positive(message = "Department ID must be a positive number") Long departmentId) {
        log.info("REST request to get active job designations for department ID: {}", departmentId);
        List<JobDesignationResponseDto> response = jobDesignationService.getActiveDesignationsByDepartment(
                departmentId
        );
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(),
                        "Department job designations retrieved successfully", response)
        );
    }
}