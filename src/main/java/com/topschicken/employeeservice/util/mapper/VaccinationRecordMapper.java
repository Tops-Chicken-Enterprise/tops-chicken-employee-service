package com.topschicken.employeeservice.util.mapper;

import com.topschicken.employeeservice.dto.request.VaccinationRecordRequestDto;
import com.topschicken.employeeservice.dto.response.VaccinationRecordResponseDto;
import com.topschicken.employeeservice.entity.Employee;
import com.topschicken.employeeservice.entity.VaccinationRecord;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface VaccinationRecordMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "empCode", source = "employee.empCode")
    @Mapping(target = "employeeName", source = "employee", qualifiedByName = "mapEmployeeName")
    @Mapping(target = "vaccineTypeId", source = "vaccineType.id")
    @Mapping(target = "vaccineTypeCode", source = "vaccineType.code")
    @Mapping(target = "vaccineTypeName", source = "vaccineType.name")
    VaccinationRecordResponseDto toResponseDto(VaccinationRecord vaccinationRecord);

    List<VaccinationRecordResponseDto> toResponseDtoList(List<VaccinationRecord> vaccinationRecords);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "vaccineType", ignore = true)
    VaccinationRecord toEntity(VaccinationRecordRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "vaccineType", ignore = true)
    void updateEntityFromDto(
            VaccinationRecordRequestDto requestDto, @MappingTarget VaccinationRecord vaccinationRecord
    );

    @Named("mapEmployeeName")
    default String mapEmployeeName(Employee employee) {
        if (employee == null) {
            return null;
        }
        String first = employee.getFirstName() != null ? employee.getFirstName() : "";
        String last = employee.getLastName() != null ? employee.getLastName() : "";
        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? null : fullName;
    }
}