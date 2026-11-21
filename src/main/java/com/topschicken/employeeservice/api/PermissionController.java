package com.topschicken.employeeservice.api;

import com.topschicken.employeeservice.dto.request.PermissionRequestDto;
import com.topschicken.employeeservice.dto.response.ApiResponseDto;
import com.topschicken.employeeservice.dto.response.PermissionResponseDto;
import com.topschicken.employeeservice.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<PermissionResponseDto>> createPermission(
            @Valid @RequestBody PermissionRequestDto requestDto) {
        log.info("REST request to create permission: {}", requestDto.getScopeCode());
        PermissionResponseDto response = permissionService.createPermission(requestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponseDto.success(HttpStatus.CREATED.value(),
                                "Permission created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponseDto<List<PermissionResponseDto>>> getAllPermissions() {
        log.info("REST request to fetch all permissions");
        List<PermissionResponseDto> response = permissionService.getAllPermissions();
        return ResponseEntity.ok(
                ApiResponseDto.success(HttpStatus.OK.value(), "Permissions fetched successfully", response)
        );
    }
}