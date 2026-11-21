package com.topschicken.employeeservice.util.mapper;

import com.topschicken.employeeservice.dto.request.JobDesignationRequestDto;
import com.topschicken.employeeservice.dto.response.JobDesignationResponseDto;
import com.topschicken.employeeservice.entity.JobDesignation;
import com.topschicken.employeeservice.entity.Permission; // අනිවාර්යයෙන්ම Permission එක Import කරන්න
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface JobDesignationMapper {

    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "departmentCode", source = "department.code")
    JobDesignationResponseDto toResponseDto(JobDesignation designation);

    List<JobDesignationResponseDto> toResponseDtoList(List<JobDesignation> designations);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    JobDesignation toEntity(JobDesignationRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "permissions", ignore = true)
    void updateEntityFromDto(JobDesignationRequestDto requestDto, @MappingTarget JobDesignation designation);

    default List<String> mapPermissions(List<Permission> permissions) {
        if (permissions == null) {
            return null;
        }
        return permissions.stream()
                .map(Permission::getScopeCode)
                .toList();
    }
}