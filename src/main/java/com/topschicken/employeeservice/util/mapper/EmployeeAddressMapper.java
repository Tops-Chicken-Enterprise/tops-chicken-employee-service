package com.topschicken.employeeservice.util.mapper;

import com.topschicken.employeeservice.dto.request.EmployeeAddressRequestDto;
import com.topschicken.employeeservice.dto.response.EmployeeAddressResponseDto;
import com.topschicken.employeeservice.entity.EmployeeAddress;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface EmployeeAddressMapper {

    EmployeeAddressResponseDto toResponseDto(EmployeeAddress address);

    List<EmployeeAddressResponseDto> toResponseDtoList(List<EmployeeAddress> addresses);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    EmployeeAddress toEntity(EmployeeAddressRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    void updateEntityFromDto(EmployeeAddressRequestDto requestDto, @MappingTarget EmployeeAddress address);
}