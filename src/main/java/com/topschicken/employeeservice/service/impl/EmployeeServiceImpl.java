package com.topschicken.employeeservice.service.impl;

import com.topschicken.employeeservice.dto.request.EmployeeRequestDto;
import com.topschicken.employeeservice.dto.response.EmployeeResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PageMetaDataDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.entity.Employee;
import com.topschicken.employeeservice.entity.JobDesignation;
import com.topschicken.employeeservice.entity.enums.EmploymentStatus;
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

        JobDesignation designation = jobDesignationRepository.findById(requestDto.getDesignationId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Designation not found with ID: " + requestDto.getDesignationId())
                );

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
            transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
            return transactionTemplate.execute(status ->
                    saveEmployeeToDb(requestDto, designation, wso2UserId));
        } catch (Exception ex) {
            log.error("Failed to save employee to local DB. Rolling back WSO2 user creation for: {}",
                    requestDto.getEmpCode(), ex);
            wso2IdentityClientService.deleteWso2UserSafely(wso2UserId, targetWso2OrgId);
            throw new RuntimeException("Database error occurred. WSO2 user creation was rolled back.", ex);
        }
    }

    public EmployeeResponseDto saveEmployeeToDb(EmployeeRequestDto requestDto, JobDesignation designation, String wso2UserId) {
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
            throw new IllegalArgumentException("An employee cannot be their own supervisor");
        }
        if (employeeRepository.existsByNicAndIdNot(requestDto.getNic(), id)) {
            throw new DuplicateResourceException("NIC already exists");
        }
        if (employeeRepository.existsByEmailIgnoreCaseAndIdNot(requestDto.getEmail(), id)) {
            throw new DuplicateResourceException("Email already exists");
        }

        JobDesignation oldDesignation = employee.getDesignation();
        JobDesignation newDesignation = oldDesignation;

        if (!oldDesignation.getId().equals(requestDto.getDesignationId())) {
            newDesignation = jobDesignationRepository.findById(requestDto.getDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation not found"));
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
            log.info("Successfully synced employee update to WSO2 for ID: {}", id);
        } catch (Exception e) {
            log.error("Failed to sync employee update to WSO2 IS for ID: {}. Local DB update was successful.", id, e);
            // Depending on strictness, you can throw or log. Logging allows DB to update while flagging WSO2 sync issue.
        }

        return employeeMapper.toResponseDto(updatedEmployee);
    }

    @Override
    public PagedResponseDto<EmployeeResponseDto> searchEmployees(
            String search, Long departmentId, EmploymentStatus status, Long farmId, Pageable pageable) {

        Page<Employee> page = employeeRepository.searchAllEmployees(search, departmentId, status, farmId, pageable);
        return PagedResponseDto.of(
                employeeMapper.toResponseDtoList(page.getContent()),
                PageMetaDataDto.from(page)
        );
    }
}