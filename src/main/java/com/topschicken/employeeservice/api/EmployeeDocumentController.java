package com.topschicken.employeeservice.api;

import com.topschicken.employeeservice.dto.request.EmployeeDocumentRequestDto;
import com.topschicken.employeeservice.dto.response.ApiResponseDto;
import com.topschicken.employeeservice.dto.response.EmployeeDocumentResponseDto;
import com.topschicken.employeeservice.entity.enums.DocumentType;
import com.topschicken.employeeservice.service.EmployeeDocumentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeDocumentController {

    private final EmployeeDocumentService documentService;

    @PostMapping("/{employeeId}/documents")
    public ResponseEntity<ApiResponseDto<EmployeeDocumentResponseDto>> createDocument(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @Valid @RequestBody EmployeeDocumentRequestDto requestDto) {
        log.info("REST request to add {} document for employee ID: {}", requestDto.getDocType(), employeeId);
        EmployeeDocumentResponseDto response = documentService.createDocument(employeeId, requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(),
                        "Employee document added successfully", response));
    }

    @PostMapping("/{employeeId}/documents/bulk")
    public ResponseEntity<ApiResponseDto<List<EmployeeDocumentResponseDto>>> createDocumentsBulk(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @RequestBody @NotEmpty(message = "Document list cannot be empty")
            List<@Valid EmployeeDocumentRequestDto> requestDtos) {
        log.info("REST request to bulk add {} documents for employee ID: {}", requestDtos.size(), employeeId);
        List<EmployeeDocumentResponseDto> response = documentService.createDocumentsBulk(employeeId, requestDtos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(HttpStatus.CREATED.value(),
                        "Employee documents added successfully", response));
    }

    @GetMapping("/{employeeId}/documents")
    public ResponseEntity<ApiResponseDto<List<EmployeeDocumentResponseDto>>> getDocumentsByEmployee(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @RequestParam(required = false) DocumentType docType) {
        List<EmployeeDocumentResponseDto> response = documentService.getDocumentsByEmployeeId(employeeId, docType);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Employee documents retrieved successfully", response));
    }

    @GetMapping("/{employeeId}/documents/{documentId}")
    public ResponseEntity<ApiResponseDto<EmployeeDocumentResponseDto>> getDocumentById(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @PathVariable @Positive(message = "Document ID must be positive") Long documentId) {
        EmployeeDocumentResponseDto response = documentService.getDocumentById(employeeId, documentId);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Employee document retrieved successfully", response));
    }

    @PutMapping("/{employeeId}/documents/{documentId}")
    public ResponseEntity<ApiResponseDto<EmployeeDocumentResponseDto>> updateDocument(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @PathVariable @Positive(message = "Document ID must be positive") Long documentId,
            @Valid @RequestBody EmployeeDocumentRequestDto requestDto) {
        log.info("REST request to update document ID: {} for employee ID: {}", documentId, employeeId);
        EmployeeDocumentResponseDto response = documentService.updateDocument(employeeId, documentId, requestDto);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Employee document updated successfully", response));
    }

    @DeleteMapping("/{employeeId}/documents/{documentId}")
    public ResponseEntity<ApiResponseDto<Void>> deleteDocument(
            @PathVariable @Positive(message = "Employee ID must be positive") Long employeeId,
            @PathVariable @Positive(message = "Document ID must be positive") Long documentId) {
        log.info("REST request to delete document ID: {} for employee ID: {}", documentId, employeeId);
        documentService.deleteDocument(employeeId, documentId);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.NO_CONTENT.value(),
                "Employee document deleted successfully", null));
    }

    @GetMapping("/documents/expiring")
    public ResponseEntity<ApiResponseDto<List<EmployeeDocumentResponseDto>>> getExpiringDocuments(
            @RequestParam(defaultValue = "30") @Positive(message = "Days must be positive") int days) {
        List<EmployeeDocumentResponseDto> response = documentService.getExpiringDocuments(days);
        return ResponseEntity.ok(ApiResponseDto.success(HttpStatus.OK.value(),
                "Expiring employee documents retrieved successfully", response));
    }
}