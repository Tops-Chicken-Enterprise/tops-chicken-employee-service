package com.topschicken.employeeservice.service.impl;

import com.topschicken.employeeservice.dto.request.EmployeeRequestDto;
import com.topschicken.employeeservice.dto.response.EmployeeResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PageMetaDataDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.entity.Employee;
import com.topschicken.employeeservice.entity.JobDesignation;
import com.topschicken.employeeservice.entity.enums.EmploymentStatus;
import com.topschicken.employeeservice.exception.BusinessValidationException;
import com.topschicken.employeeservice.exception.DuplicateResourceException;
import com.topschicken.employeeservice.exception.ResourceNotFoundException;
import com.topschicken.employeeservice.repository.EmployeeRepository;
import com.topschicken.employeeservice.repository.JobDesignationRepository;
import com.topschicken.employeeservice.service.EmployeeService;
import com.topschicken.employeeservice.service.integration.Wso2IdentityClientService;
import com.topschicken.employeeservice.util.mapper.EmployeeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final JobDesignationRepository jobDesignationRepository;
    private final EmployeeMapper employeeMapper;
    private final Wso2IdentityClientService wso2IdentityClientService;
    private final TransactionTemplate transactionTemplate;

    @Override
    public EmployeeResponseDto createEmployee(EmployeeRequestDto requestDto) {
        log.info("Creating new employee with code: {}", requestDto.getEmpCode());

        if (employeeRepository.existsByEmpCode(requestDto.getEmpCode())) {
            throw new DuplicateResourceException("Employee code already exists in Local DB");
        }
        if (employeeRepository.existsByNic(requestDto.getNic())) {
            throw new DuplicateResourceException("NIC already exists in Local DB");
        }
        if (employeeRepository.existsByEmailIgnoreCase(requestDto.getEmail())) {
            throw new DuplicateResourceException("Email already exists in Local DB");
        }
        if (requestDto.getEpfNumber() != null && !requestDto.getEpfNumber().isBlank()
                && employeeRepository.existsByEpfNumber(requestDto.getEpfNumber().trim())) {
            throw new DuplicateResourceException("EPF number already exists in Local DB");
        }

        JobDesignation designation = jobDesignationRepository.findById(requestDto.getDesignationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Designation not found with ID: " + requestDto.getDesignationId())
                );

        if (Boolean.FALSE.equals(designation.getIsActive()) ||
                Boolean.FALSE.equals(designation.getDepartment().getIsActive())) {
            throw new BusinessValidationException("Cannot assign employee to an inactive designation or department");
        }

        String targetWso2OrgId = designation.getDepartment().getWso2OrgId();

        String existingWso2UserId = wso2IdentityClientService.findUserByUsername(
                requestDto.getEmpCode(), targetWso2OrgId
        );
        if (existingWso2UserId != null) {
            throw new DuplicateResourceException("Employee code already exists in WSO2 Identity Server");
        }

        String wso2UserId = wso2IdentityClientService.createWso2User(
                requestDto.getEmpCode(),
                requestDto.getEmail(),
                requestDto.getNic(),
                requestDto.getFirstName(),
                requestDto.getLastName(),
                designation.getDepartment().getName(),
                requestDto.getContactNumber(),
                targetWso2OrgId,
                null
        );

        try {
            if (designation.getWso2GroupId() != null && !designation.getWso2GroupId().isBlank()) {
                wso2IdentityClientService.addUserToWso2Group(
                        designation.getWso2GroupId(),
                        wso2UserId,
                        requestDto.getEmpCode(),
                        targetWso2OrgId
                );
            }

            assert transactionTemplate.getTransactionManager() != null;
            TransactionTemplate requiresNewTemplate =
                    new TransactionTemplate(transactionTemplate.getTransactionManager());
            requiresNewTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            return requiresNewTemplate.execute(status ->
                    saveEmployeeToDb(requestDto, designation, wso2UserId));
        } catch (Exception ex) {
            log.error("Failed to assign group or save employee to local DB. Rolling back WSO2 user creation for: {}",
                    requestDto.getEmpCode(), ex);
            wso2IdentityClientService.deleteWso2UserSafely(wso2UserId, targetWso2OrgId);
            throw new RuntimeException(
                    "Error occurred during employee onboarding. WSO2 user creation was rolled back.", ex);
        }
    }

    public EmployeeResponseDto saveEmployeeToDb(
            EmployeeRequestDto requestDto, JobDesignation designation, String wso2UserId
    ) {
        Employee employee = employeeMapper.toEntity(requestDto);
        employee.setDesignation(designation);
        employee.setWso2UserId(wso2UserId);

        if (requestDto.getSupervisorId() != null) {
            Employee supervisor = employeeRepository.findById(requestDto.getSupervisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found"));
            employee.setSupervisor(supervisor);
        }

        Employee savedEmployee = employeeRepository.save(employee);
        log.info("Employee created successfully in DB with ID: {}", savedEmployee.getId());

        return employeeMapper.toResponseDto(savedEmployee);
    }

    @Override
    public EmployeeResponseDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findByIdWithAddresses(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));
        return employeeMapper.toResponseDto(employee);
    }

    @Override
    public EmployeeResponseDto getEmployeeByEmpCode(String empCode) {
        Employee employee = employeeRepository.findByEmpCode(empCode)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with code: " + empCode));
        return employeeMapper.toResponseDto(employee);
    }

    @Override
    @Transactional
    public EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto requestDto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + id));

        if (requestDto.getSupervisorId() != null && requestDto.getSupervisorId().equals(id)) {
            throw new BusinessValidationException("An employee cannot be their own supervisor");
        }
        if (employeeRepository.existsByNicAndIdNot(requestDto.getNic(), id)) {
            throw new DuplicateResourceException("NIC already exists");
        }
        if (employeeRepository.existsByEmailIgnoreCaseAndIdNot(requestDto.getEmail(), id)) {
            throw new DuplicateResourceException("Email already exists");
        }
        if (requestDto.getEpfNumber() != null && !requestDto.getEpfNumber().isBlank()
                && employeeRepository.existsByEpfNumberAndIdNot(requestDto.getEpfNumber().trim(), id)) {
            throw new DuplicateResourceException("EPF number already exists");
        }

        JobDesignation oldDesignation = employee.getDesignation();
        JobDesignation newDesignation = oldDesignation;
        boolean designationChanged = !oldDesignation.getId().equals(requestDto.getDesignationId());

        if (designationChanged) {
            newDesignation = jobDesignationRepository.findById(requestDto.getDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation not found"));

            if (Boolean.FALSE.equals(newDesignation.getIsActive()) ||
                    Boolean.FALSE.equals(newDesignation.getDepartment().getIsActive())) {
                throw new BusinessValidationException(
                        "Cannot assign employee to an inactive designation or department");
            }

            String oldOrgId = oldDesignation.getDepartment().getWso2OrgId();
            String newOrgId = newDesignation.getDepartment().getWso2OrgId();
            if (!Objects.equals(oldOrgId, newOrgId)) {
                throw new BusinessValidationException(
                        "Cannot change employee to a designation in" +
                                " a different department/organization via standard update"
                );
            }
            employee.setDesignation(newDesignation);
        }

        if (requestDto.getSupervisorId() != null && (employee.getSupervisor() == null || !employee.getSupervisor()
                .getId().equals(requestDto.getSupervisorId()))) {
            Employee supervisor = employeeRepository.findById(requestDto.getSupervisorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supervisor not found"));
            employee.setSupervisor(supervisor);
        }

        employeeMapper.updateEntityFromDto(requestDto, employee);
        Employee updatedEmployee = employeeRepository.saveAndFlush(employee);

        try {
            String wso2OrgId = newDesignation.getDepartment().getWso2OrgId();
            boolean isActive = updatedEmployee.getEmploymentStatus() == EmploymentStatus.ACTIVE;

            wso2IdentityClientService.updateWso2User(
                    updatedEmployee.getWso2UserId(),
                    updatedEmployee.getEmail(),
                    updatedEmployee.getNic(),
                    updatedEmployee.getFirstName(),
                    updatedEmployee.getLastName(),
                    newDesignation.getDepartment().getName(),
                    updatedEmployee.getContactNumber(),
                    isActive,
                    wso2OrgId
            );

            if (designationChanged && updatedEmployee.getWso2UserId() != null) {
                if (oldDesignation.getWso2GroupId() != null && !oldDesignation.getWso2GroupId().isBlank()) {
                    wso2IdentityClientService.removeUserFromWso2Group(
                            oldDesignation.getWso2GroupId(),
                            updatedEmployee.getWso2UserId(),
                            wso2OrgId
                    );
                }
                if (newDesignation.getWso2GroupId() != null && !newDesignation.getWso2GroupId().isBlank()) {
                    wso2IdentityClientService.addUserToWso2Group(
                            newDesignation.getWso2GroupId(),
                            updatedEmployee.getWso2UserId(),
                            updatedEmployee.getEmpCode(),
                            wso2OrgId
                    );
                }
            }

            log.info("Successfully synced employee update to WSO2 for ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to sync employee update to WSO2 IS for ID: {}. Local DB update was successful.", id, e);
        }

        return employeeMapper.toResponseDto(updatedEmployee);
    }

    @Override
    public PagedResponseDto<EmployeeResponseDto> searchEmployees(
            String search, Long departmentId, EmploymentStatus status, Long farmId, Pageable pageable) {

        String sanitizedSearch = (search == null || search.isBlank()) ? null
                : search.trim().replace(
                        "\\", "\\\\").replace("%", "\\%")
                .replace("_", "\\_");
        Page<Employee> page = employeeRepository.searchAllEmployees(
                sanitizedSearch, departmentId, status, farmId, pageable
        );

        return PagedResponseDto.of(
                employeeMapper.toResponseDtoList(page.getContent()),
                PageMetaDataDto.from(page)
        );
    }
}