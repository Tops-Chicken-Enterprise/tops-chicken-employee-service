package com.topschicken.employeeservice.service;

import com.topschicken.employeeservice.dto.request.DepartmentRequestDto;
import com.topschicken.employeeservice.dto.response.DepartmentResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepartmentService {

    DepartmentResponseDto createDepartment(DepartmentRequestDto requestDto);

    DepartmentResponseDto getDepartmentById(Long id);

    DepartmentResponseDto getDepartmentByCode(String code);

    DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto requestDto);

    DepartmentResponseDto toggleDepartmentStatus(Long id, boolean isActive);

    PagedResponseDto<DepartmentResponseDto> getAllDepartments(String search, Boolean isActive, Pageable pageable);

    List<DepartmentResponseDto> getActiveDepartments();
}