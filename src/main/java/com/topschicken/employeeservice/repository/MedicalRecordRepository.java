package com.topschicken.employeeservice.repository;

import com.topschicken.employeeservice.entity.MedicalRecord;
import com.topschicken.employeeservice.entity.enums.FitnessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    List<MedicalRecord> findAllByEmployeeIdOrderByCheckupDateDesc(Long employeeId);

    Optional<MedicalRecord> findFirstByEmployeeIdOrderByCheckupDateDesc(Long employeeId);

    List<MedicalRecord> findAllByEmployeeIdAndFitnessStatus(Long employeeId, FitnessStatus fitnessStatus);

    @Query("""
            SELECT mr FROM MedicalRecord mr
            JOIN FETCH mr.employee e
            WHERE mr.expiryDate IS NOT NULL
            AND mr.expiryDate BETWEEN :startDate AND :endDate
            ORDER BY mr.expiryDate ASC
            """)
    List<MedicalRecord> findExpiringMedicalRecords(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT mr FROM MedicalRecord mr
            JOIN FETCH mr.employee e
            LEFT JOIN FETCH mr.verifiedBy
            WHERE mr.id = :id
            """)
    Optional<MedicalRecord> findByIdWithDetails(@Param("id") Long id);
}