package com.topschicken.employeeservice.repository;

import com.topschicken.employeeservice.entity.EmployeeBankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeBankAccountRepository extends JpaRepository<EmployeeBankAccount, Long> {

    List<EmployeeBankAccount> findAllByEmployeeId(Long employeeId);

    Optional<EmployeeBankAccount> findByEmployeeIdAndIsPrimaryTrue(Long employeeId);

    boolean existsByAccountNumber(String accountNumber);

    boolean existsByAccountNumberAndIdNot(String accountNumber, Long id);

    boolean existsByEmployeeIdAndIsPrimaryTrue(Long employeeId);
}