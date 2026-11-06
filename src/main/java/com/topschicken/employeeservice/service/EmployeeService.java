package com.topschicken.employeeservice.service;

import com.topschicken.employeeservice.dto.request.EmployeeRequestDto;
import com.topschicken.employeeservice.dto.response.EmployeeResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.entity.enums.EmploymentStatus;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {

    EmployeeResponseDto createEmployee(EmployeeRequestDto requestDto);

    EmployeeResponseDto getEmployeeById(Long id);

    EmployeeResponseDto getEmployeeByEmpCode(String empCode);

    EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto requestDto);

    PagedResponseDto<EmployeeResponseDto> searchEmployees(
            String search, Long departmentId, EmploymentStatus status, Long farmId, Pageable pageable);
}