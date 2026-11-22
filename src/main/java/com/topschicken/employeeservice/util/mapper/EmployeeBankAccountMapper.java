package com.topschicken.employeeservice.util.mapper;

import com.topschicken.employeeservice.dto.request.EmployeeBankAccountRequestDto;
import com.topschicken.employeeservice.dto.response.EmployeeBankAccountResponseDto;
import com.topschicken.employeeservice.entity.EmployeeBankAccount;
import org.mapstruct.*;

import java.util.List;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        builder = @Builder(disableBuilder = true)
)
public interface EmployeeBankAccountMapper {

    EmployeeBankAccountResponseDto toResponseDto(EmployeeBankAccount bankAccount);

    List<EmployeeBankAccountResponseDto> toResponseDtoList(List<EmployeeBankAccount> bankAccounts);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    EmployeeBankAccount toEntity(EmployeeBankAccountRequestDto requestDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employee", ignore = true)
    void updateEntityFromDto(EmployeeBankAccountRequestDto requestDto, @MappingTarget EmployeeBankAccount bankAccount);
}