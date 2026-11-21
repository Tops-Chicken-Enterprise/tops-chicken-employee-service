package com.topschicken.employeeservice.service.impl;

import com.topschicken.employeeservice.dto.request.PermissionRequestDto;
import com.topschicken.employeeservice.dto.response.PermissionResponseDto;
import com.topschicken.employeeservice.entity.Permission;
import com.topschicken.employeeservice.exception.DuplicateResourceException;
import com.topschicken.employeeservice.repository.PermissionRepository;
import com.topschicken.employeeservice.service.PermissionService;
import com.topschicken.employeeservice.service.integration.Wso2IdentityClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final Wso2IdentityClientService wso2IdentityClientService;

    @Override
    @Transactional
    public PermissionResponseDto createPermission(PermissionRequestDto requestDto) {
        String scopeCode = requestDto.getScopeCode().trim().toLowerCase();

        if (permissionRepository.existsByScopeCodeIgnoreCase(scopeCode)) {
            throw new DuplicateResourceException("Permission scope '" + scopeCode + "' already exists in local DB");
        }

        wso2IdentityClientService.registerScopeInWso2(
                scopeCode,
                requestDto.getDisplayName(),
                requestDto.getDescription()
        );

        Permission permission = Permission.builder()
                .scopeCode(scopeCode)
                .displayName(requestDto.getDisplayName().trim())
                .description(requestDto.getDescription())
                .build();

        Permission savedPermission = permissionRepository.save(permission);
        log.info("Successfully saved permission '{}' in local DB", scopeCode);

        return mapToDto(savedPermission);
    }

    @Override
    public List<PermissionResponseDto> getAllPermissions() {
        return permissionRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private PermissionResponseDto mapToDto(Permission permission) {
        return PermissionResponseDto.builder()
                .id(permission.getId())
                .scopeCode(permission.getScopeCode())
                .displayName(permission.getDisplayName())
                .description(permission.getDescription())
                .build();
    }
}