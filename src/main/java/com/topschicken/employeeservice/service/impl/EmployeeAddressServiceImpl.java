package com.topschicken.employeeservice.service.impl;

import com.topschicken.employeeservice.dto.request.EmployeeAddressRequestDto;
import com.topschicken.employeeservice.dto.response.EmployeeAddressResponseDto;
import com.topschicken.employeeservice.entity.Employee;
import com.topschicken.employeeservice.entity.EmployeeAddress;
import com.topschicken.employeeservice.exception.DuplicateResourceException;
import com.topschicken.employeeservice.exception.ResourceNotFoundException;
import com.topschicken.employeeservice.repository.EmployeeAddressRepository;
import com.topschicken.employeeservice.repository.EmployeeRepository;
import com.topschicken.employeeservice.service.EmployeeAddressService;
import com.topschicken.employeeservice.util.mapper.EmployeeAddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeAddressServiceImpl implements EmployeeAddressService {

    private final EmployeeAddressRepository addressRepository;
    private final EmployeeRepository employeeRepository;
    private final EmployeeAddressMapper addressMapper;

    @Override
    @Transactional
    public EmployeeAddressResponseDto createAddress(Long employeeId, EmployeeAddressRequestDto requestDto) {
        log.info("Adding {} address for employee ID: {}", requestDto.getAddressType(), employeeId);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + employeeId));

        if (addressRepository.existsByEmployeeIdAndAddressType(employeeId, requestDto.getAddressType())) {
            throw new DuplicateResourceException(
                    String.format("Employee ID %d already has a %s address", employeeId, requestDto.getAddressType())
            );
        }

        EmployeeAddress address = addressMapper.toEntity(requestDto);
        address.setEmployee(employee);
        EmployeeAddress saved = addressRepository.save(address);

        log.info("Successfully added address ID: {} for employee ID: {}", saved.getId(), employeeId);
        return addressMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeAddressResponseDto> getAddressesByEmployeeId(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }

        return addressMapper.toResponseDtoList(addressRepository.findAllByEmployeeId(employeeId));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeAddressResponseDto getAddressById(Long employeeId, Long addressId) {
        EmployeeAddress address = getOwnedAddressOrThrow(employeeId, addressId);
        return addressMapper.toResponseDto(address);
    }

    @Override
    @Transactional
    public EmployeeAddressResponseDto updateAddress(
            Long employeeId, Long addressId, EmployeeAddressRequestDto requestDto)
    {
        log.info("Updating address ID: {} for employee ID: {}", addressId, employeeId);

        EmployeeAddress address = getOwnedAddressOrThrow(employeeId, addressId);

        if (addressRepository.existsByEmployeeIdAndAddressTypeAndIdNot(
                employeeId, requestDto.getAddressType(), addressId)) {
            throw new DuplicateResourceException(
                    String.format("Employee ID %d already has another %s address",
                            employeeId, requestDto.getAddressType())
            );
        }

        addressMapper.updateEntityFromDto(requestDto, address);
        EmployeeAddress updated = addressRepository.saveAndFlush(address);

        log.info("Successfully updated address ID: {} for employee ID: {}", addressId, employeeId);
        return addressMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteAddress(Long employeeId, Long addressId) {
        log.info("Deleting address ID: {} for employee ID: {}", addressId, employeeId);

        EmployeeAddress address = getOwnedAddressOrThrow(employeeId, addressId);
        addressRepository.delete(address);

        log.info("Successfully deleted address ID: {} for employee ID: {}", addressId, employeeId);
    }

    private EmployeeAddress getOwnedAddressOrThrow(Long employeeId, Long addressId) {
        EmployeeAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found with ID: " + addressId));

        if (!address.getEmployee().getId().equals(employeeId)) {
            throw new ResourceNotFoundException(
                    String.format("Address ID %d does not belong to employee ID %d", addressId, employeeId)
            );
        }
        return address;
    }
}