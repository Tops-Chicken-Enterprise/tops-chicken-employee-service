package com.topschicken.employeeservice.repository;

import com.topschicken.employeeservice.entity.JobDesignation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobDesignationRepository extends JpaRepository<JobDesignation, Long> {

    Optional<JobDesignation> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<JobDesignation> findAllByDepartmentIdAndIsActiveTrueOrderByTitleAsc(Long departmentId);

    boolean existsByDepartmentIdAndIsActiveTrue(Long departmentId);

    @Query("SELECT jd FROM JobDesignation jd JOIN FETCH jd.department WHERE jd.isActive = true ORDER BY jd.title ASC")
    List<JobDesignation> findAllActiveWithDepartment();

    @Query("SELECT jd FROM JobDesignation jd JOIN FETCH jd.department WHERE jd.id = :id")
    Optional<JobDesignation> findByIdWithDepartment(@Param("id") Long id);

    @Query(value = """
            SELECT jd FROM JobDesignation jd
            JOIN FETCH jd.department d
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(jd.code) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(jd.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:departmentId IS NULL OR d.id = :departmentId)
            """,
            countQuery = """
            SELECT COUNT(jd) FROM JobDesignation jd
            JOIN jd.department d
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(jd.code) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(jd.title) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:departmentId IS NULL OR d.id = :departmentId)
            """)
    Page<JobDesignation> searchAllJobDesignations(
            @Param("search") String search,
            @Param("departmentId") Long departmentId,
            Pageable pageable
    );
}