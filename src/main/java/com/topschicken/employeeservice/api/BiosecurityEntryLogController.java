package com.topschicken.employeeservice.api;

import com.topschicken.employeeservice.dto.request.BiosecurityEntryLogRequestDto;
import com.topschicken.employeeservice.dto.response.ApiResponseDto;
import com.topschicken.employeeservice.dto.response.BiosecurityEntryLogResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.entity.enums.SanitizationStatus;
import com.topschicken.employeeservice.service.BiosecurityEntryLogService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequestMapping("/biosecurity-logs")
@RequiredArgsConstructor
public class BiosecurityEntryLogController {

    private final BiosecurityEntryLogService entryLogService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<BiosecurityEntryLogResponseDto>> createEntryLog(
            @Valid @RequestBody BiosecurityEntryLogRequestDto requestDto) {
        log.info("REST request to create biosecurity entry log for employee ID: {}", requestDto.getEmployeeId());
        BiosecurityEntryLogResponseDto response = entryLogService.createEntryLog(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(),
                        "Biosecurity entry log created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PagedResponseDto<BiosecurityEntryLogResponseDto>>> getAllEntryLogs(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number must be 0 or greater") int page,
            @RequestParam(defaultValue = "10") @Positive(message = "Page size must be positive") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "entryTime"));
        PagedResponseDto<BiosecurityEntryLogResponseDto> response = entryLogService.getAllEntryLogs(pageable);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Biosecurity entry logs retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<BiosecurityEntryLogResponseDto>> getEntryLogById(
            @PathVariable @Positive(message = "Log ID must be positive") Long id) {
        BiosecurityEntryLogResponseDto response = entryLogService.getEntryLogById(id);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Biosecurity entry log retrieved successfully", response));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponseDto<List<BiosecurityEntryLogResponseDto>>> getEntryLogsByEmployee(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId) {
        List<BiosecurityEntryLogResponseDto> response = entryLogService.getEntryLogsByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Employee biosecurity entry logs retrieved successfully", response));
    }

    @GetMapping("/farm/{farmId}")
    public ResponseEntity<ApiResponseDto<PagedResponseDto<BiosecurityEntryLogResponseDto>>> getLogsByFarmAndDateRange(
            @PathVariable @Positive(message = "Farm ID must be positive") Long farmId,
            @RequestParam(required = false) @Positive(message = "Shed ID must be positive") Long shedId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number must be 0 or greater") int page,
            @RequestParam(defaultValue = "10") @Positive(message = "Page size must be positive") int size) {
        Pageable pageable = PageRequest.of(page, size);
        PagedResponseDto<BiosecurityEntryLogResponseDto> response =
                entryLogService.getLogsByFarmAndDateRange(farmId, shedId, startTime, endTime, pageable);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Farm biosecurity entry logs retrieved successfully", response));
    }

    @GetMapping("/incidents")
    public ResponseEntity<ApiResponseDto<List<BiosecurityEntryLogResponseDto>>> getRecentIncidents(
            @RequestParam(required = false) SanitizationStatus status,
            @RequestParam(defaultValue = "24") @Positive(message = "Hours must be positive") int hours) {
        List<BiosecurityEntryLogResponseDto> response = entryLogService.getRecentIncidents(status, hours);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Recent biosecurity incidents retrieved successfully", response));
    }

    @PatchMapping("/{id}/exit")
    public ResponseEntity<ApiResponseDto<BiosecurityEntryLogResponseDto>> recordExitTime(
            @PathVariable @Positive(message = "Log ID must be positive") Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime exitTime)
    {
        log.info("REST request to record exit time for biosecurity log ID: {}", id);
        BiosecurityEntryLogResponseDto response = entryLogService.recordExitTime(id, exitTime);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Biosecurity exit time recorded successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<BiosecurityEntryLogResponseDto>> updateEntryLog(
            @PathVariable @Positive(message = "Log ID must be positive") Long id,
            @Valid @RequestBody BiosecurityEntryLogRequestDto requestDto) {
        log.info("REST request to update biosecurity entry log ID: {}", id);
        BiosecurityEntryLogResponseDto response = entryLogService.updateEntryLog(id, requestDto);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Biosecurity entry log updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteEntryLog(
            @PathVariable @Positive(message = "Log ID must be positive") Long id) {
        log.info("REST request to delete biosecurity entry log ID: {}", id);
        entryLogService.deleteEntryLog(id);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.NO_CONTENT.value(),
                "Biosecurity entry log deleted successfully", null));
    }
}