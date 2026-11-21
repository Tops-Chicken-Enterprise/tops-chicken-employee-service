package com.topschicken.employeeservice.service.impl;

import com.topschicken.employeeservice.dto.request.DepartmentRequestDto;
import com.topschicken.employeeservice.dto.response.DepartmentResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PageMetaDataDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.entity.Department;
import com.topschicken.employeeservice.exception.BusinessValidationException;
import com.topschicken.employeeservice.exception.DuplicateResourceException;
import com.topschicken.employeeservice.exception.ResourceNotFoundException;
import com.topschicken.employeeservice.repository.DepartmentRepository;
import com.topschicken.employeeservice.repository.JobDesignationRepository;
import com.topschicken.employeeservice.service.DepartmentService;
import com.topschicken.employeeservice.service.integration.Wso2IdentityClientService;
import com.topschicken.employeeservice.util.mapper.DepartmentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final JobDesignationRepository jobDesignationRepository;
    private final DepartmentMapper departmentMapper;
    private final Wso2IdentityClientService wso2IdentityClientService;

    @Override
    @Transactional
    public DepartmentResponseDto createDepartment(DepartmentRequestDto requestDto) {
        log.info("Creating new department with code: {}", requestDto.getCode());

        String normalizedCode = requestDto.getCode().trim().toUpperCase();
        String normalizedName = requestDto.getName().trim();

        if (departmentRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new DuplicateResourceException("Department with code '" + normalizedCode + "' already exists");
        }

        if (departmentRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateResourceException("Department with name '" + normalizedName + "' already exists");
        }

        String wso2OrgId = wso2IdentityClientService.createWso2Organization(
                normalizedName,
                requestDto.getDescription()
        );

        try {
            Department department = departmentMapper.toEntity(requestDto);
            department.setCode(normalizedCode);
            department.setName(normalizedName);
            department.setIsActive(true);
            department.setWso2OrgId(wso2OrgId);

            Department savedDepartment = departmentRepository.saveAndFlush(department);
            log.info(
                    "Department created successfully with ID: {} and WSO2 Org ID: {}",
                    savedDepartment.getId(), wso2OrgId
            );

            return departmentMapper.toResponseDto(savedDepartment);
        } catch (Exception ex) {
            log.error("Failed to save department to local DB. Rolling back WSO2 Organization ID: {}", wso2OrgId, ex);
            wso2IdentityClientService.deleteWso2OrganizationSafely(wso2OrgId);
            throw ex;
        }
    }

    @Override
    public DepartmentResponseDto getDepartmentById(Long id) {
        log.debug("Fetching department by ID: {}", id);
        Department department = findDepartmentById(id);
        return departmentMapper.toResponseDto(department);
    }

    @Override
    public DepartmentResponseDto getDepartmentByCode(String code) {
        log.debug("Fetching department by code: {}", code);
        Department department = departmentRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with code: " + code));
        return departmentMapper.toResponseDto(department);
    }

    @Override
    @Transactional
    public DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto requestDto) {
        log.info("Updating department with ID: {}", id);
        Department department = findDepartmentById(id);

        String normalizedCode = requestDto.getCode().trim().toUpperCase();
        String normalizedName = requestDto.getName().trim();

        if (departmentRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
            throw new DuplicateResourceException("Department with code '" + normalizedCode + "' already exists");
        }

        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(normalizedName, id)) {
            throw new DuplicateResourceException("Department with name '" + normalizedName + "' already exists");
        }

        if (Boolean.FALSE.equals(requestDto.getIsActive()) && Boolean.TRUE.equals(department.getIsActive())) {
            if (jobDesignationRepository.existsByDepartmentIdAndIsActiveTrue(id)) {
                throw new BusinessValidationException(
                        "Cannot deactivate department because it has active job designations"
                );
            }
        }

        departmentMapper.updateEntityFromDto(requestDto, department);
        department.setCode(normalizedCode);
        department.setName(normalizedName);

        Department updatedDepartment = departmentRepository.saveAndFlush(department);
        if (updatedDepartment.getWso2OrgId() != null && !updatedDepartment.getWso2OrgId().isBlank()) {
            wso2IdentityClientService.updateWso2Organization(
                    updatedDepartment.getWso2OrgId(),
                    updatedDepartment.getName(),
                    updatedDepartment.getDescription(),
                    Boolean.TRUE.equals(updatedDepartment.getIsActive())
            );
        }
        log.info("Department updated successfully with ID: {}", updatedDepartment.getId());

        return departmentMapper.toResponseDto(updatedDepartment);
    }

    @Override
    @Transactional
    public DepartmentResponseDto toggleDepartmentStatus(Long id, boolean isActive) {
        log.info("Toggling status for department ID: {} to isActive: {}", id, isActive);
        Department department = findDepartmentById(id);

        if (!isActive && jobDesignationRepository.existsByDepartmentIdAndIsActiveTrue(id)) {
            throw new BusinessValidationException(
                    "Cannot deactivate department because it has active job designations"
            );
        }

        department.setIsActive(isActive);
        Department updatedDepartment = departmentRepository.saveAndFlush(department);

        if (updatedDepartment.getWso2OrgId() != null && !updatedDepartment.getWso2OrgId().isBlank()) {
            wso2IdentityClientService.updateWso2Organization(
                    updatedDepartment.getWso2OrgId(),
                    updatedDepartment.getName(),
                    updatedDepartment.getDescription(),
                    isActive
            );
        }

        return departmentMapper.toResponseDto(updatedDepartment);
    }

    @Override
    public PagedResponseDto<DepartmentResponseDto> getAllDepartments(String search, Boolean isActive, Pageable pageable)
    {
        String sanitizedSearch = sanitizeSearchTerm(search);
        log.debug("Searching departments with sanitized search: '{}', isActive: '{}', page: {}",
                sanitizedSearch, isActive, pageable.getPageNumber());

        Page<Department> departmentPage = departmentRepository.searchAllDepartments(
                sanitizedSearch, isActive, pageable
        );
        List<DepartmentResponseDto> dtoList = departmentMapper.toResponseDtoList(departmentPage.getContent());

        return PagedResponseDto.of(dtoList, PageMetaDataDto.from(departmentPage));
    }

    @Override
    public List<DepartmentResponseDto> getActiveDepartments() {
        log.debug("Fetching all active departments for dropdown selection");
        List<Department> activeDepartments = departmentRepository.findAllByIsActiveTrueOrderByNameAsc();
        return departmentMapper.toResponseDtoList(activeDepartments);
    }

    private Department findDepartmentById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with ID: " + id));
    }

    private String sanitizeSearchTerm(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search.trim()
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}