package com.topschicken.employeeservice.api;

import com.topschicken.employeeservice.dto.request.MedicalRecordRequestDto;
import com.topschicken.employeeservice.dto.response.ApiResponseDto;
import com.topschicken.employeeservice.dto.response.MedicalRecordResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.entity.enums.FitnessStatus;
import com.topschicken.employeeservice.service.MedicalRecordService;
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
@RequestMapping("/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<MedicalRecordResponseDto>> createMedicalRecord(
            @Valid @RequestBody MedicalRecordRequestDto requestDto) {
        log.info("REST request to create medical record for employee ID: {}", requestDto.getEmployeeId());
        MedicalRecordResponseDto response = medicalRecordService.createMedicalRecord(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(),
                        "Medical record created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PagedResponseDto<MedicalRecordResponseDto>>> getAllMedicalRecords(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number must be 0 or greater") int page,
            @RequestParam(defaultValue = "10") @Positive(message = "Page size must be positive") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "checkupDate"));
        PagedResponseDto<MedicalRecordResponseDto> response = medicalRecordService.getAllMedicalRecords(pageable);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Medical records retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<MedicalRecordResponseDto>> getMedicalRecordById(
            @PathVariable @Positive(message = "Medical record ID must be positive") Long id) {
        MedicalRecordResponseDto response = medicalRecordService.getMedicalRecordById(id);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Medical record retrieved successfully", response));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponseDto<List<MedicalRecordResponseDto>>> getMedicalRecordsByEmployee(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @RequestParam(required = false) FitnessStatus fitnessStatus) {
        List<MedicalRecordResponseDto> response =
                medicalRecordService.getMedicalRecordsByEmployeeId(employeeId, fitnessStatus);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Employee medical records retrieved successfully", response));
    }

    @GetMapping("/employee/{employeeId}/latest")
    public ResponseEntity<ApiResponseDto<MedicalRecordResponseDto>> getLatestMedicalRecordByEmployee(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId) {
        MedicalRecordResponseDto response = medicalRecordService.getLatestMedicalRecordByEmployeeId(employeeId);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Latest employee medical record retrieved successfully", response));
    }

    @GetMapping("/expiring")
    public ResponseEntity<ApiResponseDto<List<MedicalRecordResponseDto>>> getExpiringMedicalRecords(
            @RequestParam(defaultValue = "30") @Positive(message = "Days must be positive") int days) {
        List<MedicalRecordResponseDto> response = medicalRecordService.getExpiringMedicalRecords(days);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Expiring medical records retrieved successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<MedicalRecordResponseDto>> updateMedicalRecord(
            @PathVariable @Positive(message = "Medical record ID must be positive") Long id,
            @Valid @RequestBody MedicalRecordRequestDto requestDto) {
        log.info("REST request to update medical record ID: {}", id);
        MedicalRecordResponseDto response = medicalRecordService.updateMedicalRecord(id, requestDto);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Medical record updated successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteMedicalRecord(
            @PathVariable @Positive(message = "Medical record ID must be positive") Long id) {
        log.info("REST request to delete medical record ID: {}", id);
        medicalRecordService.deleteMedicalRecord(id);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.NO_CONTENT.value(),
                "Medical record deleted successfully", null));
    }
}