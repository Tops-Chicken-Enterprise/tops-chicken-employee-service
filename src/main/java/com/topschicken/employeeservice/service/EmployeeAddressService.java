package com.topschicken.employeeservice.service;

import com.topschicken.employeeservice.dto.request.EmployeeAddressRequestDto;
import com.topschicken.employeeservice.dto.response.EmployeeAddressResponseDto;

import java.util.List;

public interface EmployeeAddressService {

    EmployeeAddressResponseDto createAddress(Long employeeId, EmployeeAddressRequestDto requestDto);

    List<EmployeeAddressResponseDto> getAddressesByEmployeeId(Long employeeId);

    EmployeeAddressResponseDto getAddressById(Long employeeId, Long addressId);

    EmployeeAddressResponseDto updateAddress(Long employeeId, Long addressId, EmployeeAddressRequestDto requestDto);

    void deleteAddress(Long employeeId, Long addressId);
}