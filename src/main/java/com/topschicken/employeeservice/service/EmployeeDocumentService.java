package com.topschicken.employeeservice.service;

import com.topschicken.employeeservice.dto.request.EmployeeDocumentRequestDto;
import com.topschicken.employeeservice.dto.response.EmployeeDocumentResponseDto;
import com.topschicken.employeeservice.entity.enums.DocumentType;

import java.util.List;

public interface EmployeeDocumentService {

    EmployeeDocumentResponseDto createDocument(Long employeeId, EmployeeDocumentRequestDto requestDto);

    List<EmployeeDocumentResponseDto> createDocumentsBulk(
            Long employeeId, List<EmployeeDocumentRequestDto> requestDtos
    );

    List<EmployeeDocumentResponseDto> getDocumentsByEmployeeId(Long employeeId, DocumentType docType);

    EmployeeDocumentResponseDto getDocumentById(Long employeeId, Long documentId);

    EmployeeDocumentResponseDto updateDocument(Long employeeId, Long documentId, EmployeeDocumentRequestDto requestDto);

    void deleteDocument(Long employeeId, Long documentId);

    List<EmployeeDocumentResponseDto> getExpiringDocuments(int days);
}