package com.topschicken.employeeservice.service;

import com.topschicken.employeeservice.dto.request.EmployeeBankAccountRequestDto;
import com.topschicken.employeeservice.dto.response.EmployeeBankAccountResponseDto;

import java.util.List;

public interface EmployeeBankAccountService {

    EmployeeBankAccountResponseDto createBankAccount(Long employeeId, EmployeeBankAccountRequestDto requestDto);

    List<EmployeeBankAccountResponseDto> getBankAccountsByEmployeeId(Long employeeId);

    EmployeeBankAccountResponseDto getBankAccountById(Long employeeId, Long bankAccountId);

    EmployeeBankAccountResponseDto updateBankAccount(
            Long employeeId, Long bankAccountId, EmployeeBankAccountRequestDto requestDto
    );

    void deleteBankAccount(Long employeeId, Long bankAccountId);
}