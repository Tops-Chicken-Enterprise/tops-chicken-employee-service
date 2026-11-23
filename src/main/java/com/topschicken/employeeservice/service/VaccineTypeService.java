package com.topschicken.employeeservice.service;

import com.topschicken.employeeservice.dto.request.VaccineTypeRequestDto;
import com.topschicken.employeeservice.dto.response.VaccineTypeResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VaccineTypeService {

    VaccineTypeResponseDto createVaccineType(VaccineTypeRequestDto requestDto);

    List<VaccineTypeResponseDto> getAllVaccineTypes(Boolean mandatoryOnly);

    VaccineTypeResponseDto getVaccineTypeById(Long id);

    VaccineTypeResponseDto getVaccineTypeByCode(String code);

    VaccineTypeResponseDto updateVaccineType(Long id, VaccineTypeRequestDto requestDto);

    PagedResponseDto<VaccineTypeResponseDto> getAllVaccineTypes(Boolean mandatoryOnly, Pageable pageable);

    void deleteVaccineType(Long id);

}