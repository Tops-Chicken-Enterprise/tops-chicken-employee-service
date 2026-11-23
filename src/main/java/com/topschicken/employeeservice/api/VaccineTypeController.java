package com.topschicken.employeeservice.api;

import com.topschicken.employeeservice.dto.request.VaccineTypeRequestDto;
import com.topschicken.employeeservice.dto.response.ApiResponseDto;
import com.topschicken.employeeservice.dto.response.VaccineTypeResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.service.VaccineTypeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
@RequestMapping("vaccine-types")
@RequiredArgsConstructor
public class VaccineTypeController {

    private final VaccineTypeService vaccineTypeService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<VaccineTypeResponseDto>> createVaccineType(
            @Valid @RequestBody VaccineTypeRequestDto requestDto) {
        log.info("REST request to create vaccine type: {}", requestDto.getCode());
        VaccineTypeResponseDto response = vaccineTypeService.createVaccineType(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(),
                        "Vaccine type created successfully", response));
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponseDto<List<VaccineTypeResponseDto>>> getAllVaccineTypes(
            @RequestParam(required = false) Boolean mandatoryOnly) {
        List<VaccineTypeResponseDto> response = vaccineTypeService.getAllVaccineTypes(mandatoryOnly);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Vaccine types retrieved successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<VaccineTypeResponseDto>> getVaccineTypeById(
            @PathVariable @Positive(message = "Vaccine type ID must be positive") Long id) {
        VaccineTypeResponseDto response = vaccineTypeService.getVaccineTypeById(id);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Vaccine type retrieved successfully", response));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponseDto<VaccineTypeResponseDto>> getVaccineTypeByCode(
            @PathVariable @NotBlank(message = "Vaccine code cannot be blank") String code) {
        VaccineTypeResponseDto response = vaccineTypeService.getVaccineTypeByCode(code);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Vaccine type retrieved successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<VaccineTypeResponseDto>> updateVaccineType(
            @PathVariable @Positive(message = "Vaccine type ID must be positive") Long id,
            @Valid @RequestBody VaccineTypeRequestDto requestDto) {
        log.info("REST request to update vaccine type ID: {}", id);
        VaccineTypeResponseDto response = vaccineTypeService.updateVaccineType(id, requestDto);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Vaccine type updated successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<PagedResponseDto<VaccineTypeResponseDto>>> getAllVaccineTypes(
            @RequestParam(required = false) Boolean mandatoryOnly,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page number must be 0 or greater") int page,
            @RequestParam(defaultValue = "10") @Positive(message = "Page size must be positive") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        PagedResponseDto<VaccineTypeResponseDto> response =
                vaccineTypeService.getAllVaccineTypes(mandatoryOnly, pageable);

        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Vaccine types retrieved successfully", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<Void>> deleteVaccineType(
            @PathVariable @Positive(message = "Vaccine type ID must be positive") Long id) {
        log.info("REST request to delete vaccine type ID: {}", id);
        vaccineTypeService.deleteVaccineType(id);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.NO_CONTENT.value(),
                "Vaccine type deleted successfully", null));
    }
}