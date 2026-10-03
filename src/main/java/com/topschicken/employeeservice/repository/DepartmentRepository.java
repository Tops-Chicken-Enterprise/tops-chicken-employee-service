package com.topschicken.employeeservice.repository;

import com.topschicken.employeeservice.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

    List<Department> findAllByIsActiveTrueOrderByNameAsc();

    @Query(value = """
            SELECT d FROM Department d
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:isActive IS NULL OR d.isActive = :isActive)
            """,
            countQuery = """
            SELECT COUNT(d) FROM Department d
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(d.code) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(d.name) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:isActive IS NULL OR d.isActive = :isActive)
            """)
    Page<Department> searchAllDepartments(
            @Param("search") String search,
            @Param("isActive") Boolean isActive,
            Pageable pageable
    );
}