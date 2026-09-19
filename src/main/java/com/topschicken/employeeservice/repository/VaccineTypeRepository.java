package com.topschicken.employeeservice.repository;

import com.topschicken.employeeservice.entity.VaccineType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VaccineTypeRepository extends JpaRepository<VaccineType, Long> {

    Optional<VaccineType> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, Long id);

    List<VaccineType> findAllByIsMandatoryTrueOrderByNameAsc();
}