package com.topschicken.employeeservice.api;

import com.topschicken.employeeservice.dto.request.VaccinationRecordRequestDto;
import com.topschicken.employeeservice.dto.response.ApiResponseDto;
import com.topschicken.employeeservice.dto.response.VaccinationRecordResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.entity.enums.VaccinationStatus;
import com.topschicken.employeeservice.service.VaccinationRecordService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/vaccination-records")
@RequiredArgsConstructor
public class VaccinationRecordController {

    private final VaccinationRecordService vaccinationRecordService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<VaccinationRecordResponseDto>> createVaccinationRecord(
            @Valid @RequestBody VaccinationRecordRequestDto requestDto) {
        log.info("REST request to create vaccination record for employee ID: {}", requestDto.getEmployeeId());
        VaccinationRecordResponseDto response = vaccinationRecordService.createVaccinationRecord(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(),
                        "Vaccination record created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PagedResponseDto<VaccinationRecordResponseDto>>> getAllVaccinationRecords(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number must be 0 or greater") int page,
            @RequestParam(defaultValue = "10") @Positive(message = "Page size must be positive") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "administeredDate"));
        PagedResponseDto<VaccinationRecordResponseDto> response =
                vaccinationRecordService.getAllVaccinationRecords(pageable);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Vaccination records retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<VaccinationRecordResponseDto>> getVaccinationRecordById(
            @PathVariable @Positive(message = "Vaccination record ID must be positive") Long id) {
        VaccinationRecordResponseDto response = vaccinationRecordService.getVaccinationRecordById(id);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Vaccination record retrieved successfully", response));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponseDto<List<VaccinationRecordResponseDto>>> getVaccinationRecordsByEmployee(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @RequestParam(required = false) @Positive(message = "Vaccine type ID must be positive")
            Long vaccineTypeId) {
        List<VaccinationRecordResponseDto> response =
                vaccinationRecordService.getVaccinationRecordsByEmployeeId(employeeId, vaccineTypeId);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Employee vaccination records retrieved successfully", response));
    }

    @GetMapping("/expiring")
    public ResponseEntity<ApiResponseDto<List<VaccinationRecordResponseDto>>> getExpiringVaccinations(
            @RequestParam(defaultValue = "30") @Positive(message = "Days must be positive") int days,
            @RequestParam(required = false) VaccinationStatus status) {
        List<VaccinationRecordResponseDto> response =
                vaccinationRecordService.getExpiringVaccinations(days, status);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Expiring vaccination records retrieved successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<VaccinationRecordResponseDto>> updateVaccinationRecord(
            @PathVariable @Positive(message = "Vaccination record ID must be positive") Long id,
            @Valid @RequestBody VaccinationRecordRequestDto requestDto) {
        log.info("REST request to update vaccination record ID: {}", id);
        VaccinationRecordResponseDto response = vaccinationRecordService.updateVaccinationRecord(id, requestDto);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Vaccination record updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteVaccinationRecord(
            @PathVariable @Positive(message = "Vaccination record ID must be positive") Long id) {
        log.info("REST request to delete vaccination record ID: {}", id);
        vaccinationRecordService.deleteVaccinationRecord(id);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.NO_CONTENT.value(),
                "Vaccination record deleted successfully", null));
    }
}