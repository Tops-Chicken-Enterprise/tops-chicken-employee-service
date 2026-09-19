package com.topschicken.employeeservice.repository;

import com.topschicken.employeeservice.entity.EmployeeAddress;
import com.topschicken.employeeservice.entity.enums.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeAddressRepository extends JpaRepository<EmployeeAddress, Long> {

    List<EmployeeAddress> findAllByEmployeeId(Long employeeId);

    Optional<EmployeeAddress> findByEmployeeIdAndAddressType(Long employeeId, AddressType addressType);

    boolean existsByEmployeeIdAndAddressType(Long employeeId, AddressType addressType);

    boolean existsByEmployeeIdAndAddressTypeAndIdNot(Long employeeId, AddressType addressType, Long id);
}