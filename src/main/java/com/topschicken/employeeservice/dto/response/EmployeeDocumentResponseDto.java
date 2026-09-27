package com.topschicken.employeeservice.dto.response;

import com.topschicken.employeeservice.entity.enums.DocumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDocumentResponseDto {
    private Long id;
    private DocumentType docType;
    private String docTitle;
    private String fileUrl;
    private LocalDate expiryDate;
    private Long uploadedById;
    private String uploadedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}