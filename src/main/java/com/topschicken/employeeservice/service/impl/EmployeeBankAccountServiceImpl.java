package com.topschicken.employeeservice.service.impl;

import com.topschicken.employeeservice.dto.request.EmployeeBankAccountRequestDto;
import com.topschicken.employeeservice.dto.response.EmployeeBankAccountResponseDto;
import com.topschicken.employeeservice.entity.Employee;
import com.topschicken.employeeservice.entity.EmployeeBankAccount;
import com.topschicken.employeeservice.exception.DuplicateResourceException;
import com.topschicken.employeeservice.exception.ResourceNotFoundException;
import com.topschicken.employeeservice.repository.EmployeeBankAccountRepository;
import com.topschicken.employeeservice.repository.EmployeeRepository;
import com.topschicken.employeeservice.service.EmployeeBankAccountService;
import com.topschicken.employeeservice.util.mapper.EmployeeBankAccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeBankAccountServiceImpl implements EmployeeBankAccountService {

    private final EmployeeBankAccountRepository bankAccountRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeBankAccountMapper bankAccountMapper;

    @Override
    @Transactional
    public EmployeeBankAccountResponseDto createBankAccount(Long employeeId, EmployeeBankAccountRequestDto requestDto) {
        log.info("Adding bank account for employee ID: {}", employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        String accountNumber = requestDto.getAccountNumber().trim();
        if (bankAccountRepository.existsByAccountNumber(accountNumber)) {
            throw new DuplicateResourceException("Bank account number already exists: " + accountNumber);
        }

        if (Boolean.TRUE.equals(requestDto.getIsPrimary())) {
            demoteExistingPrimaryAccount(employeeId, null);
        }

        EmployeeBankAccount bankAccount = bankAccountMapper.toEntity(requestDto);
        bankAccount.setAccountNumber(accountNumber);
        bankAccount.setEmployee(employee);

        EmployeeBankAccount saved = bankAccountRepository.save(bankAccount);
        log.info("Successfully added bank account ID: {} for employee ID: {}", saved.getId(), employeeId);
        return bankAccountMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeBankAccountResponseDto> getBankAccountsByEmployeeId(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }
        return bankAccountMapper.toResponseDtoList(bankAccountRepository.findAllByEmployeeId(employeeId));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeBankAccountResponseDto getBankAccountById(Long employeeId, Long bankAccountId) {
        EmployeeBankAccount bankAccount = getOwnedBankAccountOrThrow(employeeId, bankAccountId);
        return bankAccountMapper.toResponseDto(bankAccount);
    }

    @Override
    @Transactional
    public EmployeeBankAccountResponseDto updateBankAccount(
            Long employeeId, Long bankAccountId, EmployeeBankAccountRequestDto requestDto
    ) {
        log.info("Updating bank account ID: {} for employee ID: {}", bankAccountId, employeeId);

        EmployeeBankAccount bankAccount = getOwnedBankAccountOrThrow(employeeId, bankAccountId);

        String accountNumber = requestDto.getAccountNumber().trim();
        if (bankAccountRepository.existsByAccountNumberAndIdNot(accountNumber, bankAccountId)) {
            throw new DuplicateResourceException("Bank account number already exists: " + accountNumber);
        }

        if (Boolean.TRUE.equals(requestDto.getIsPrimary())) {
            demoteExistingPrimaryAccount(employeeId, bankAccountId);
        }

        bankAccountMapper.updateEntityFromDto(requestDto, bankAccount);
        bankAccount.setAccountNumber(accountNumber);

        EmployeeBankAccount updated = bankAccountRepository.saveAndFlush(bankAccount);
        log.info("Successfully updated bank account ID: {} for employee ID: {}", bankAccountId, employeeId);
        return bankAccountMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteBankAccount(Long employeeId, Long bankAccountId) {
        log.info("Deleting bank account ID: {} for employee ID: {}", bankAccountId, employeeId);

        EmployeeBankAccount bankAccount = getOwnedBankAccountOrThrow(employeeId, bankAccountId);
        bankAccountRepository.delete(bankAccount);

        log.info("Successfully deleted bank account ID: {} for employee ID: {}", bankAccountId, employeeId);
    }

    private void demoteExistingPrimaryAccount(Long employeeId, Long excludeAccountId) {
        bankAccountRepository.findByEmployeeIdAndIsPrimaryTrue(employeeId)
                .ifPresent(existingPrimary -> {
                    if (excludeAccountId == null || !existingPrimary.getId().equals(excludeAccountId)) {
                        existingPrimary.setIsPrimary(false);
                        bankAccountRepository.save(existingPrimary);
                    }
                });
    }

    private EmployeeBankAccount getOwnedBankAccountOrThrow(Long employeeId, Long bankAccountId) {
        EmployeeBankAccount bankAccount = bankAccountRepository.findById(bankAccountId)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account not found with ID: " + bankAccountId));

        if (!bankAccount.getEmployee().getId().equals(employeeId)) {
            throw new ResourceNotFoundException(
                    String.format("Bank account ID %d does not belong to employee ID %d", bankAccountId, employeeId)
            );
        }
        return bankAccount;
    }
}