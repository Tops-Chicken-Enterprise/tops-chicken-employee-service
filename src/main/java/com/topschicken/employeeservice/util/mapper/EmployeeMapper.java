package com.topschicken.employeeservice.util.mapper;

import com.topschicken.employeeservice.dto.request.EmployeeAddressRequestDto;
import com.topschicken.employeeservice.dto.request.EmployeeBankAccountRequestDto;
import com.topschicken.employeeservice.dto.request.EmployeeDocumentRequestDto;
import com.topschicken.employeeservice.dto.request.EmployeeRequestDto;
import com.topschicken.employeeservice.dto.response.EmployeeAddressResponseDto;
import com.topschicken.employeeservice.dto.response.EmployeeBankAccountResponseDto;
import com.topschicken.employeeservice.dto.response.EmployeeDocumentResponseDto;
import com.topschicken.employeeservice.dto.response.EmployeeResponseDto;
import com.topschicken.employeeservice.entity.Employee;
import com.topschicken.employeeservice.entity.EmployeeAddress;
import com.topschicken.employeeservice.entity.EmployeeBankAccount;
import com.topschicken.employeeservice.entity.EmployeeDocument;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface EmployeeMapper {

    @Mapping(target = "fullName", expression = "java(constructFullName(employee))")
    @Mapping(target = "designationId", source = "designation.id")
    @Mapping(target = "designationTitle", source = "designation.title")
    @Mapping(target = "departmentId", source = "designation.department.id")
    @Mapping(target = "departmentName", source = "designation.department.name")
    @Mapping(target = "supervisorId", source = "supervisor.id")
    @Mapping(target = "supervisorName", expression = "java(constructSupervisorName(employee.getSupervisor()))")
    EmployeeResponseDto toResponseDto(Employee employee);

    List<EmployeeResponseDto> toResponseDtoList(List<Employee> employees);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "designation", ignore = true)
    @Mapping(target = "supervisor", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "bankAccounts", ignore = true)
    @Mapping(target = "documents", ignore = true)
    @Mapping(target = "medicalRecords", ignore = true)
    @Mapping(target = "vaccinationRecords", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Employee toEntity(EmployeeRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "empCode", ignore = true) // General updates should not modify empCode
    @Mapping(target = "designation", ignore = true)
    @Mapping(target = "supervisor", ignore = true)
    @Mapping(target = "addresses", ignore = true)
    @Mapping(target = "bankAccounts", ignore = true)
    @Mapping(target = "documents", ignore = true)
    void updateEntityFromDto(EmployeeRequestDto requestDto, @MappingTarget Employee employee);

    // Sub-Mappers
    EmployeeAddressResponseDto addressToResponseDto(EmployeeAddress address);
    List<EmployeeAddressResponseDto> addressToResponseDtoList(List<EmployeeAddress> addresses);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    EmployeeAddress addressRequestToEntity(EmployeeAddressRequestDto requestDto);

    EmployeeBankAccountResponseDto bankToResponseDto(EmployeeBankAccount bankAccount);
    List<EmployeeBankAccountResponseDto> bankToResponseDtoList(List<EmployeeBankAccount> bankAccounts);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    EmployeeBankAccount bankRequestToEntity(EmployeeBankAccountRequestDto requestDto);

    EmployeeDocumentResponseDto docToResponseDto(EmployeeDocument document);
    List<EmployeeDocumentResponseDto> docToResponseDtoList(List<EmployeeDocument> documents);
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    @Mapping(target = "uploadedBy", ignore = true)
    EmployeeDocument docRequestToEntity(EmployeeDocumentRequestDto requestDto);


    // Helper methods for custom mapping logic
    default String constructFullName(Employee employee) {
        if (employee == null) return null;
        StringBuilder fullName = new StringBuilder();
        if (employee.getFirstName() != null) fullName.append(employee.getFirstName());
        if (employee.getMiddleName() != null && !employee.getMiddleName().trim().isEmpty()) {
            fullName.append(" ").append(employee.getMiddleName());
        }
        if (employee.getLastName() != null) fullName.append(" ").append(employee.getLastName());
        return fullName.toString().trim();
    }

    default String constructSupervisorName(Employee supervisor) {
        return constructFullName(supervisor);
    }
}