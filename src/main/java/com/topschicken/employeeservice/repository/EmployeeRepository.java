package com.topschicken.employeeservice.repository;

import com.topschicken.employeeservice.entity.Employee;
import com.topschicken.employeeservice.entity.enums.EmploymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long>, JpaSpecificationExecutor<Employee> {

    Optional<Employee> findByEmpCode(String empCode);

    Optional<Employee> findByNic(String nic);

    Optional<Employee> findByEmailIgnoreCase(String email);

    Optional<Employee> findByWso2UserId(String wso2UserId);

    Optional<Employee> findByFarmIdAndBiometricId(Long farmId, Integer biometricId);

    boolean existsByEmpCode(String empCode);
    boolean existsByNic(String nic);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEpfNumber(String epfNumber);

    boolean existsByEmpCodeAndIdNot(String empCode, Long id);
    boolean existsByNicAndIdNot(String nic, Long id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByEpfNumberAndIdNot(String epfNumber, Long id);

    boolean existsByFarmIdAndBiometricId(Long farmId, Integer biometricId);
    boolean existsByFarmIdAndBiometricIdAndIdNot(Long farmId, Integer biometricId, Long id);

    boolean existsByDesignationId(Long designationId);
    boolean existsByDesignationIdAndEmploymentStatusNot(Long designationId, EmploymentStatus status);
    boolean existsByDesignationIdAndResignedDateIsNull(Long designationId);
    List<Employee> findAllBySupervisorIdAndEmploymentStatus(Long supervisorId, EmploymentStatus status);



    @Query(value = """
            SELECT e FROM Employee e
            JOIN FETCH e.designation d
            JOIN FETCH d.department
            WHERE (:status IS NULL OR e.employmentStatus = :status)
            AND (:farmId IS NULL OR e.farmId = :farmId)
            """,
            countQuery = """
            SELECT COUNT(e) FROM Employee e
            WHERE (:status IS NULL OR e.employmentStatus = :status)
            AND (:farmId IS NULL OR e.farmId = :farmId)
            """)
    Page<Employee> findAllEmployeesWithDesignationAndDepartment(
            @Param("status") EmploymentStatus status,
            @Param("farmId") Long farmId,
            Pageable pageable
    );

    @Query("""
            SELECT e FROM Employee e
            JOIN FETCH e.designation d
            JOIN FETCH d.department
            LEFT JOIN FETCH e.supervisor
            WHERE e.id = :id
            """)
    Optional<Employee> findByIdWithDesignationAndSupervisor(@Param("id") Long id);

    @Query("""
            SELECT DISTINCT e FROM Employee e
            JOIN FETCH e.designation d
            JOIN FETCH d.department
            LEFT JOIN FETCH e.supervisor
            LEFT JOIN FETCH e.addresses
            WHERE e.id = :id
            """)
    Optional<Employee> findByIdWithAddresses(@Param("id") Long id);


    @Query("""
            SELECT DISTINCT e FROM Employee e
            LEFT JOIN FETCH e.bankAccounts
            WHERE e.id = :id
            """)
    Optional<Employee> findByIdWithBankAccounts(@Param("id") Long id);

    @Query(value = """
            SELECT e FROM Employee e
            JOIN FETCH e.designation d
            JOIN FETCH d.department dept
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(e.empCode) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.nic) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:departmentId IS NULL OR dept.id = :departmentId)
            AND (:status IS NULL OR e.employmentStatus = :status)
            AND (:farmId IS NULL OR e.farmId = :farmId)
            """,
            countQuery = """
            SELECT COUNT(e) FROM Employee e
            JOIN e.designation d
            JOIN d.department dept
            WHERE (:search IS NULL OR :search = '' OR
                   LOWER(e.empCode) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.nic) LIKE LOWER(CONCAT('%', :search, '%')) OR
                   LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')))
            AND (:departmentId IS NULL OR dept.id = :departmentId)
            AND (:status IS NULL OR e.employmentStatus = :status)
            AND (:farmId IS NULL OR e.farmId = :farmId)
            """)
    Page<Employee> searchAllEmployees(
            @Param("search") String search,
            @Param("departmentId") Long departmentId,
            @Param("status") EmploymentStatus status,
            @Param("farmId") Long farmId,
            Pageable pageable
    );
}