package com.topschicken.employeeservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobDesignationResponseDto {
    private Long id;
    private Long departmentId;
    private String departmentName;
    private String departmentCode;
    private String code;
    private String title;
    private String description;
    private List<String> permissions;
    private String wso2GroupId;
    private String wso2RoleId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}