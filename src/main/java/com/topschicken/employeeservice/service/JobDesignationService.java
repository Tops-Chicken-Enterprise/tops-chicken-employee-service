package com.topschicken.employeeservice.service;

import com.topschicken.employeeservice.dto.request.JobDesignationRequestDto;
import com.topschicken.employeeservice.dto.response.JobDesignationResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobDesignationService {

    JobDesignationResponseDto createDesignation(JobDesignationRequestDto requestDto);

    JobDesignationResponseDto getDesignationById(Long id);

    JobDesignationResponseDto getDesignationByCode(String code);

    JobDesignationResponseDto updateDesignation(Long id, JobDesignationRequestDto requestDto);

    JobDesignationResponseDto toggleDesignationStatus(Long id, boolean isActive);

    PagedResponseDto<JobDesignationResponseDto> getAllDesignations(
            String search, Long departmentId, Boolean isActive, Pageable pageable
    );

    List<JobDesignationResponseDto> getActiveDesignations();

    List<JobDesignationResponseDto> getActiveDesignationsByDepartment(Long departmentId);
}