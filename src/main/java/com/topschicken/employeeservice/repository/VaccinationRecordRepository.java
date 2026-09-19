package com.topschicken.employeeservice.repository;

import com.topschicken.employeeservice.entity.VaccinationRecord;
import com.topschicken.employeeservice.entity.enums.VaccinationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VaccinationRecordRepository extends JpaRepository<VaccinationRecord, Long> {

    @Query("""
            SELECT vr FROM VaccinationRecord vr
            JOIN FETCH vr.vaccineType vt
            WHERE vr.employee.id = :employeeId
            ORDER BY vr.administeredDate DESC
            """)
    List<VaccinationRecord> findAllByEmployeeIdWithVaccineType(@Param("employeeId") Long employeeId);

    boolean existsByEmployeeIdAndVaccineTypeIdAndDoseNumber(Long employeeId, Long vaccineTypeId, Integer doseNumber);

    List<VaccinationRecord> findAllByEmployeeIdAndVaccineTypeId(Long employeeId, Long vaccineTypeId);

    @Query("""
            SELECT vr FROM VaccinationRecord vr
            JOIN FETCH vr.employee e
            JOIN FETCH vr.vaccineType vt
            WHERE vr.expiryDate IS NOT NULL
            AND vr.status = :status
            AND vr.expiryDate BETWEEN :startDate AND :endDate
            ORDER BY vr.expiryDate ASC
            """)
    List<VaccinationRecord> findExpiringVaccinations(
            @Param("status") VaccinationStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}