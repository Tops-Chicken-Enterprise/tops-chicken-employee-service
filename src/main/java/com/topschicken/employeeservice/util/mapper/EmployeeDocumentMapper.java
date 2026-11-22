package com.topschicken.employeeservice.util.mapper;

import com.topschicken.employeeservice.dto.request.EmployeeDocumentRequestDto;
import com.topschicken.employeeservice.dto.response.EmployeeDocumentResponseDto;
import com.topschicken.employeeservice.entity.Employee;
import com.topschicken.employeeservice.entity.EmployeeDocument;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface EmployeeDocumentMapper {

    @Mapping(target = "uploadedById", source = "uploadedBy.id")
    @Mapping(target = "uploadedByName", source = "uploadedBy", qualifiedByName = "mapUploadedByName")
    EmployeeDocumentResponseDto toResponseDto(EmployeeDocument document);

    List<EmployeeDocumentResponseDto> toResponseDtoList(List<EmployeeDocument> documents);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    EmployeeDocument toEntity(EmployeeDocumentRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    void updateEntityFromDto(EmployeeDocumentRequestDto requestDto, @MappingTarget EmployeeDocument document);

    @Named("mapUploadedByName")
    default String mapUploadedByName(Employee uploadedBy) {
        if (uploadedBy == null) {
            return null;
        }
        String first = uploadedBy.getFirstName() != null ? uploadedBy.getFirstName() : "";
        String last = uploadedBy.getLastName() != null ? uploadedBy.getLastName() : "";
        String fullName = (first + " " + last).trim();
        return fullName.isEmpty() ? null : fullName;
    }
}