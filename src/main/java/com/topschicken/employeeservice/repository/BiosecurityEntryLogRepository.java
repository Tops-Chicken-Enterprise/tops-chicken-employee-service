package com.topschicken.employeeservice.repository;

import com.topschicken.employeeservice.entity.BiosecurityEntryLog;
import com.topschicken.employeeservice.entity.enums.SanitizationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BiosecurityEntryLogRepository extends JpaRepository<BiosecurityEntryLog, Long> {

    List<BiosecurityEntryLog> findAllByEmployeeIdOrderByEntryTimeDesc(Long employeeId);

    @Query(value = """
            SELECT bl FROM BiosecurityEntryLog bl
            JOIN FETCH bl.employee e
            LEFT JOIN FETCH bl.clearedBy
            WHERE bl.farmId = :farmId
            AND (:shedId IS NULL OR bl.shedId = :shedId)
            AND bl.entryTime BETWEEN :startTime AND :endTime
            ORDER BY bl.entryTime DESC
            """,
            countQuery = """
            SELECT COUNT(bl) FROM BiosecurityEntryLog bl
            WHERE bl.farmId = :farmId
            AND (:shedId IS NULL OR bl.shedId = :shedId)
            AND bl.entryTime BETWEEN :startTime AND :endTime
            """)
    Page<BiosecurityEntryLog> findLogsByFarmAndDateRange(
            @Param("farmId") Long farmId,
            @Param("shedId") Long shedId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );

    @Query("""
            SELECT bl FROM BiosecurityEntryLog bl
            JOIN FETCH bl.employee e
            LEFT JOIN FETCH bl.clearedBy
            WHERE bl.sanitizationStatus = :status
            AND bl.entryTime >= :since
            ORDER BY bl.entryTime DESC
            """)
    List<BiosecurityEntryLog> findRecentIncidents(
            @Param("status") SanitizationStatus status,
            @Param("since") LocalDateTime since
    );
}