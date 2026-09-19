package com.topschicken.employeeservice.repository;

import com.topschicken.employeeservice.entity.EmployeeDocument;
import com.topschicken.employeeservice.entity.enums.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EmployeeDocumentRepository extends JpaRepository<EmployeeDocument, Long> {

    List<EmployeeDocument> findAllByEmployeeId(Long employeeId);

    List<EmployeeDocument> findAllByEmployeeIdAndDocType(Long employeeId, DocumentType docType);

    @Query("""
            SELECT ed FROM EmployeeDocument ed
            JOIN FETCH ed.employee e
            WHERE ed.expiryDate IS NOT NULL
            AND ed.expiryDate BETWEEN :startDate AND :endDate
            ORDER BY ed.expiryDate ASC
            """)
    List<EmployeeDocument> findExpiringDocuments(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}