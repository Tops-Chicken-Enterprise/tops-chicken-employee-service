package com.topschicken.employeeservice.service.impl;

import com.topschicken.employeeservice.dto.request.EmployeeDocumentRequestDto;
import com.topschicken.employeeservice.dto.response.EmployeeDocumentResponseDto;
import com.topschicken.employeeservice.entity.Employee;
import com.topschicken.employeeservice.entity.EmployeeDocument;
import com.topschicken.employeeservice.entity.enums.DocumentType;
import com.topschicken.employeeservice.exception.ResourceNotFoundException;
import com.topschicken.employeeservice.repository.EmployeeDocumentRepository;
import com.topschicken.employeeservice.repository.EmployeeRepository;
import com.topschicken.employeeservice.service.EmployeeDocumentService;
import com.topschicken.employeeservice.util.mapper.EmployeeDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeDocumentServiceImpl implements EmployeeDocumentService {

    private final EmployeeDocumentRepository documentRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeDocumentMapper documentMapper;

    @Override
    @Transactional
    public EmployeeDocumentResponseDto createDocument(Long employeeId, EmployeeDocumentRequestDto requestDto) {
        log.info("Adding {} document for employee ID: {}", requestDto.getDocType(), employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        EmployeeDocument document = buildDocumentEntity(employee, requestDto);
        EmployeeDocument saved = documentRepository.save(document);

        log.info("Successfully added document ID: {} for employee ID: {}", saved.getId(), employeeId);
        return documentMapper.toResponseDto(saved);
    }

    @Override
    @Transactional
    public List<EmployeeDocumentResponseDto> createDocumentsBulk(
            Long employeeId, List<EmployeeDocumentRequestDto> requestDtos
    ) {
        log.info("Adding {} documents in bulk for employee ID: {}", requestDtos.size(), employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        List<EmployeeDocument> documentsToSave = new ArrayList<>();
        for (EmployeeDocumentRequestDto dto : requestDtos) {
            documentsToSave.add(buildDocumentEntity(employee, dto));
        }

        List<EmployeeDocument> savedDocuments = documentRepository.saveAll(documentsToSave);
        log.info("Successfully added {} documents for employee ID: {}", savedDocuments.size(), employeeId);
        return documentMapper.toResponseDtoList(savedDocuments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDocumentResponseDto> getDocumentsByEmployeeId(Long employeeId, DocumentType docType) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }

        List<EmployeeDocument> documents = (docType != null)
                ? documentRepository.findAllByEmployeeIdAndDocType(employeeId, docType)
                : documentRepository.findAllByEmployeeId(employeeId);

        return documentMapper.toResponseDtoList(documents);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDocumentResponseDto getDocumentById(Long employeeId, Long documentId) {
        EmployeeDocument document = getOwnedDocumentOrThrow(employeeId, documentId);
        return documentMapper.toResponseDto(document);
    }

    @Override
    @Transactional
    public EmployeeDocumentResponseDto updateDocument(
            Long employeeId, Long documentId, EmployeeDocumentRequestDto requestDto
    ) {
        log.info("Updating document ID: {} for employee ID: {}", documentId, employeeId);

        EmployeeDocument document = getOwnedDocumentOrThrow(employeeId, documentId);
        documentMapper.updateEntityFromDto(requestDto, document);

        if (requestDto.getUploadedById() != null) {
            Employee uploader = employeeRepository.findById(requestDto.getUploadedById())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Uploader employee not found with ID: " + requestDto.getUploadedById()));
            document.setUploadedBy(uploader);
        }

        EmployeeDocument updated = documentRepository.saveAndFlush(document);
        log.info("Successfully updated document ID: {} for employee ID: {}", documentId, employeeId);
        return documentMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteDocument(Long employeeId, Long documentId) {
        log.info("Deleting document ID: {} for employee ID: {}", documentId, employeeId);

        EmployeeDocument document = getOwnedDocumentOrThrow(employeeId, documentId);
        documentRepository.delete(document);

        log.info("Successfully deleted document ID: {} for employee ID: {}", documentId, employeeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDocumentResponseDto> getExpiringDocuments(int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        return documentMapper.toResponseDtoList(documentRepository.findExpiringDocuments(today, endDate));
    }

    private EmployeeDocument buildDocumentEntity(Employee employee, EmployeeDocumentRequestDto requestDto) {
        EmployeeDocument document = documentMapper.toEntity(requestDto);
        document.setEmployee(employee);

        if (requestDto.getUploadedById() != null) {
            Employee uploader = employeeRepository.findById(requestDto.getUploadedById())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Uploader employee not found with ID: " + requestDto.getUploadedById()));
            document.setUploadedBy(uploader);
        }
        return document;
    }

    private EmployeeDocument getOwnedDocumentOrThrow(Long employeeId, Long documentId) {
        EmployeeDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId));

        if (!document.getEmployee().getId().equals(employeeId)) {
            throw new ResourceNotFoundException(
                    String.format("Document ID %d does not belong to employee ID %d", documentId, employeeId)
            );
        }
        return document;
    }
}