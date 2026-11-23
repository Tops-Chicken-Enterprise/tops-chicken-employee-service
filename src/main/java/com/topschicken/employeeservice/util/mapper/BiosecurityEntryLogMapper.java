package com.topschicken.employeeservice.util.mapper;

import com.topschicken.employeeservice.dto.request.BiosecurityEntryLogRequestDto;
import com.topschicken.employeeservice.dto.response.BiosecurityEntryLogResponseDto;
import com.topschicken.employeeservice.entity.BiosecurityEntryLog;
import com.topschicken.employeeservice.entity.Employee;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface BiosecurityEntryLogMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "empCode", source = "employee.empCode")
    @Mapping(target = "employeeName", source = "employee", qualifiedByName = "mapFullName")
    @Mapping(target = "clearedById", source = "clearedBy.id")
    @Mapping(target = "clearedByName", source = "clearedBy", qualifiedByName = "mapFullName")
    BiosecurityEntryLogResponseDto toResponseDto(BiosecurityEntryLog logEntry);

    List<BiosecurityEntryLogResponseDto> toResponseDtoList(List<BiosecurityEntryLog> logEntries);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "clearedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    BiosecurityEntryLog toEntity(BiosecurityEntryLogRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "clearedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(BiosecurityEntryLogRequestDto requestDto, @MappingTarget BiosecurityEntryLog logEntry);

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