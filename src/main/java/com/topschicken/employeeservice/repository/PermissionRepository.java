package com.topschicken.employeeservice.repository;

import com.topschicken.employeeservice.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    boolean existsByScopeCodeIgnoreCase(String scopeCode);
    Optional<Permission> findByScopeCode(String scopeCode);
    List<Permission> findByScopeCodeIn(List<String> scopeCodes);
}