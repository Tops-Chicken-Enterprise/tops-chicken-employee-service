package com.topschicken.employeeservice.util.mapper;

import com.topschicken.employeeservice.dto.request.MedicalRecordRequestDto;
import com.topschicken.employeeservice.dto.response.MedicalRecordResponseDto;
import com.topschicken.employeeservice.entity.Employee;
import com.topschicken.employeeservice.entity.MedicalRecord;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface MedicalRecordMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "empCode", source = "employee.empCode")
    @Mapping(target = "employeeName", source = "employee", qualifiedByName = "mapFullName")
    @Mapping(target = "verifiedById", source = "verifiedBy.id")
    @Mapping(target = "verifiedByName", source = "verifiedBy", qualifiedByName = "mapFullName")
    MedicalRecordResponseDto toResponseDto(MedicalRecord medicalRecord);

    List<MedicalRecordResponseDto> toResponseDtoList(List<MedicalRecord> medicalRecords);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "verifiedBy", ignore = true)
    MedicalRecord toEntity(MedicalRecordRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "verifiedBy", ignore = true)
    void updateEntityFromDto(MedicalRecordRequestDto requestDto, @MappingTarget MedicalRecord medicalRecord);

    @Named("mapFullName")
    default String mapFullName(Employee employee) {
        if (employee == null) {
            return null;
        }
        String first = employee.getFirstName() != null ? employee.getFirstName() : "";
        String last = employee.getLastName() != null ? employee.getLastName() : "";
        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? null : fullName;
    }
}