package com.topschicken.employeeservice.util.mapper;

import com.topschicken.employeeservice.dto.request.VaccineTypeRequestDto;
import com.topschicken.employeeservice.dto.response.VaccineTypeResponseDto;
import com.topschicken.employeeservice.entity.VaccineType;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface VaccineTypeMapper {

    VaccineTypeResponseDto toResponseDto(VaccineType vaccineType);

    List<VaccineTypeResponseDto> toResponseDtoList(List<VaccineType> vaccineTypes);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vaccinationRecords", ignore = true)
    VaccineType toEntity(VaccineTypeRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "vaccinationRecords", ignore = true)
    void updateEntityFromDto(VaccineTypeRequestDto requestDto, @MappingTarget VaccineType vaccineType);
}