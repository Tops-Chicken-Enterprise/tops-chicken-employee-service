package com.topschicken.employeeservice.util.mapper;

import com.topschicken.employeeservice.dto.request.DepartmentRequestDto;
import com.topschicken.employeeservice.dto.response.DepartmentResponseDto;
import com.topschicken.employeeservice.entity.Department;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface DepartmentMapper {

    DepartmentResponseDto toResponseDto(Department department);

    List<DepartmentResponseDto> toResponseDtoList(List<Department> departments);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "designations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", ignore = true)
    Department toEntity(DepartmentRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "designations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDto(DepartmentRequestDto requestDto, @MappingTarget Department department);
}