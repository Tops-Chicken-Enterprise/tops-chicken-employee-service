package com.topschicken.employeeservice.service.impl;

import com.topschicken.employeeservice.dto.request.JobDesignationRequestDto;
import com.topschicken.employeeservice.dto.response.JobDesignationResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PageMetaDataDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.entity.Department;
import com.topschicken.employeeservice.entity.JobDesignation;
import com.topschicken.employeeservice.exception.BusinessValidationException;
import com.topschicken.employeeservice.exception.DuplicateResourceException;
import com.topschicken.employeeservice.exception.ResourceNotFoundException;
import com.topschicken.employeeservice.repository.DepartmentRepository;
import com.topschicken.employeeservice.repository.EmployeeRepository;
import com.topschicken.employeeservice.repository.JobDesignationRepository;
import com.topschicken.employeeservice.service.JobDesignationService;
import com.topschicken.employeeservice.util.mapper.JobDesignationMapper;
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
public class JobDesignationServiceImpl implements JobDesignationService {

    private final JobDesignationRepository jobDesignationRepository;
    private final DepartmentRepository departmentRepository;
    private final EmployeeRepository employeeRepository;
    private final JobDesignationMapper jobDesignationMapper;

    @Override
    @Transactional
    public JobDesignationResponseDto createDesignation(JobDesignationRequestDto requestDto) {
        log.info("Creating new job designation with code: {}", requestDto.getCode());

        String normalizedCode = requestDto.getCode().trim().toUpperCase();
        String normalizedTitle = requestDto.getTitle().trim();

        if (jobDesignationRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new DuplicateResourceException("Job designation with code '" + normalizedCode + "' already exists");
        }

        if (jobDesignationRepository.existsByDepartmentIdAndTitleIgnoreCase(
                requestDto.getDepartmentId(), normalizedTitle)
        ) {
            throw new DuplicateResourceException(
                    "Job designation with title '" + normalizedTitle + "' already exists in this department"
            );
        }

        Department department = departmentRepository.findById(requestDto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + requestDto.getDepartmentId())
                );

        if (Boolean.FALSE.equals(department.getIsActive())) {
            throw new BusinessValidationException("Cannot create designation under an inactive department");
        }

        JobDesignation designation = jobDesignationMapper.toEntity(requestDto);
        designation.setCode(normalizedCode);
        designation.setTitle(normalizedTitle);
        designation.setDepartment(department);
        designation.setIsActive(true);

        JobDesignation savedDesignation = jobDesignationRepository.save(designation);
        log.info("Job designation created successfully with ID: {}", savedDesignation.getId());

        return jobDesignationMapper.toResponseDto(savedDesignation);
    }

    @Override
    public JobDesignationResponseDto getDesignationById(Long id) {
        log.debug("Fetching job designation by ID: {}", id);
        JobDesignation designation = findDesignationByIdWithDepartment(id);
        return jobDesignationMapper.toResponseDto(designation);
    }

    @Override
    public JobDesignationResponseDto getDesignationByCode(String code) {
        log.debug("Fetching job designation by code: {}", code);
        JobDesignation designation = jobDesignationRepository.findByCodeIgnoreCaseWithDepartment(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Job designation not found with code: " + code));
        return jobDesignationMapper.toResponseDto(designation);
    }

    @Override
    @Transactional
    public JobDesignationResponseDto updateDesignation(Long id, JobDesignationRequestDto requestDto) {
        log.info("Updating job designation with ID: {}", id);
        JobDesignation designation = findDesignationByIdWithDepartment(id);

        String normalizedCode = requestDto.getCode().trim().toUpperCase();
        String normalizedTitle = requestDto.getTitle().trim();

        if (jobDesignationRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
            throw new DuplicateResourceException("Job designation with code '" + normalizedCode + "' already exists");
        }

        if (jobDesignationRepository.existsByDepartmentIdAndTitleIgnoreCaseAndIdNot(
                requestDto.getDepartmentId(), normalizedTitle, id)
        ) {
            throw new DuplicateResourceException(
                    "Job designation with title '" + normalizedTitle + "' already exists in this department"
            );
        }

        if (!designation.getDepartment().getId().equals(requestDto.getDepartmentId())) {
            Department newDepartment = departmentRepository.findById(requestDto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with ID: " + requestDto.getDepartmentId())
                    );

            if (Boolean.FALSE.equals(newDepartment.getIsActive())) {
                throw new BusinessValidationException("Cannot move designation to an inactive department");
            }
            designation.setDepartment(newDepartment);
        }

        if (Boolean.FALSE.equals(requestDto.getIsActive()) && Boolean.TRUE.equals(designation.getIsActive())) {
            validateNoActiveEmployeesAssigned(id);
        }

        jobDesignationMapper.updateEntityFromDto(requestDto, designation);
        designation.setCode(normalizedCode);
        designation.setTitle(normalizedTitle);

        JobDesignation updatedDesignation = jobDesignationRepository.saveAndFlush(designation);
        log.info("Job designation updated successfully with ID: {}", updatedDesignation.getId());

        return jobDesignationMapper.toResponseDto(updatedDesignation);
    }

    @Override
    @Transactional
    public JobDesignationResponseDto toggleDesignationStatus(Long id, boolean isActive) {
        log.info("Toggling status for designation ID: {} to isActive: {}", id, isActive);
        JobDesignation designation = findDesignationByIdWithDepartment(id);

        if (isActive && Boolean.FALSE.equals(designation.getDepartment().getIsActive())) {
            throw new BusinessValidationException("Cannot activate designation because its department is inactive");
        }

        if (!isActive) {
            validateNoActiveEmployeesAssigned(id);
        }

        designation.setIsActive(isActive);
        JobDesignation updatedDesignation = jobDesignationRepository.saveAndFlush(designation);

        return jobDesignationMapper.toResponseDto(updatedDesignation);
    }

    @Override
    public PagedResponseDto<JobDesignationResponseDto> getAllDesignations(
            String search, Long departmentId, Boolean isActive, Pageable pageable
    ) {
        String sanitizedSearch = sanitizeSearchTerm(search);
        log.debug("Searching designations with sanitized search: '{}', departmentId: {}, isActive: {}, page: {}",
                sanitizedSearch, departmentId, isActive, pageable.getPageNumber());

        Page<JobDesignation> designationPage = jobDesignationRepository.searchAllJobDesignations(
                sanitizedSearch, departmentId, isActive, pageable
        );
        List<JobDesignationResponseDto> dtoList = jobDesignationMapper.toResponseDtoList(designationPage.getContent());

        return PagedResponseDto.of(dtoList, PageMetaDataDto.from(designationPage));
    }

    @Override
    public List<JobDesignationResponseDto> getActiveDesignations() {
        log.debug("Fetching all active designations with department info");
        List<JobDesignation> activeDesignations = jobDesignationRepository.findAllActiveWithDepartment();
        return jobDesignationMapper.toResponseDtoList(activeDesignations);
    }

    @Override
    public List<JobDesignationResponseDto> getActiveDesignationsByDepartment(Long departmentId) {
        log.debug("Fetching active designations for department ID: {}", departmentId);
        List<JobDesignation> designations = jobDesignationRepository.
                findAllByDepartmentIdAndIsActiveTrueOrderByTitleAsc(departmentId);
        return jobDesignationMapper.toResponseDtoList(designations);
    }

    private JobDesignation findDesignationByIdWithDepartment(Long id) {
        return jobDesignationRepository.findByIdWithDepartment(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job designation not found with ID: " + id));
    }

    private void validateNoActiveEmployeesAssigned(Long designationId) {
        if (employeeRepository.existsByDesignationIdAndResignedDateIsNull(designationId)) {
            throw new BusinessValidationException(
                    "Cannot deactivate job designation because active employees are currently assigned to it"
            );
        }
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