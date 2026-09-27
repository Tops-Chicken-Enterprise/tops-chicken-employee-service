package com.topschicken.employeeservice.dto.request;

import com.topschicken.employeeservice.entity.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDocumentRequestDto {

    @NotNull(message = "Document type is mandatory")
    private DocumentType docType;

    @NotBlank(message = "Document title is mandatory")
    @Size(max = 150, message = "Document title must not exceed 150 characters")
    private String docTitle;

    @NotBlank(message = "File URL is mandatory")
    @Size(max = 255, message = "File URL must not exceed 255 characters")
    private String fileUrl;

    private LocalDate expiryDate;

    private Long uploadedById;
}