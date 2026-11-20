package com.topschicken.employeeservice.service;

import com.topschicken.employeeservice.dto.request.PermissionRequestDto;
import com.topschicken.employeeservice.dto.response.PermissionResponseDto;

import java.util.List;

public interface PermissionService {

    PermissionResponseDto createPermission(PermissionRequestDto requestDto);

    List<PermissionResponseDto> getAllPermissions();
}