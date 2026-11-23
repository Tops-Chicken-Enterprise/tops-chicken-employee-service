package com.topschicken.employeeservice.service.impl;

import com.topschicken.employeeservice.dto.request.VaccineTypeRequestDto;
import com.topschicken.employeeservice.dto.response.VaccineTypeResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PageMetaDataDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.entity.VaccineType;
import com.topschicken.employeeservice.exception.DuplicateResourceException;
import com.topschicken.employeeservice.exception.ResourceNotFoundException;
import com.topschicken.employeeservice.repository.VaccineTypeRepository;
import com.topschicken.employeeservice.service.VaccineTypeService;
import com.topschicken.employeeservice.util.mapper.VaccineTypeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaccineTypeServiceImpl implements VaccineTypeService {

    private final VaccineTypeRepository vaccineTypeRepository;
    private final VaccineTypeMapper vaccineTypeMapper;

    @Override
    @Transactional
    public VaccineTypeResponseDto createVaccineType(VaccineTypeRequestDto requestDto) {
        String normalizedCode = requestDto.getCode().trim().toUpperCase();
        log.info("Creating vaccine type with code: {}", normalizedCode);

        if (vaccineTypeRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new DuplicateResourceException("Vaccine type already exists with code: " + normalizedCode);
        }

        VaccineType vaccineType = vaccineTypeMapper.toEntity(requestDto);
        vaccineType.setCode(normalizedCode);
        vaccineType.setName(requestDto.getName().trim());

        VaccineType saved = vaccineTypeRepository.save(vaccineType);
        log.info("Successfully created vaccine type ID: {} ({})", saved.getId(), saved.getCode());
        return vaccineTypeMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VaccineTypeResponseDto> getAllVaccineTypes(Boolean mandatoryOnly) {
        List<VaccineType> vaccineTypes = Boolean.TRUE.equals(mandatoryOnly)
                ? vaccineTypeRepository.findAllByIsMandatoryTrueOrderByNameAsc()
                : vaccineTypeRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));

        return vaccineTypeMapper.toResponseDtoList(vaccineTypes);
    }

    @Override
    @Transactional(readOnly = true)
    public VaccineTypeResponseDto getVaccineTypeById(Long id) {
        VaccineType vaccineType = vaccineTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccine type not found with ID: " + id));
        return vaccineTypeMapper.toResponseDto(vaccineType);
    }

    @Override
    @Transactional(readOnly = true)
    public VaccineTypeResponseDto getVaccineTypeByCode(String code) {
        VaccineType vaccineType = vaccineTypeRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Vaccine type not found with code: " + code));
        return vaccineTypeMapper.toResponseDto(vaccineType);
    }

    @Override
    @Transactional
    public VaccineTypeResponseDto updateVaccineType(Long id, VaccineTypeRequestDto requestDto) {
        log.info("Updating vaccine type ID: {}", id);

        VaccineType vaccineType = vaccineTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccine type not found with ID: " + id));

        String normalizedCode = requestDto.getCode().trim().toUpperCase();
        if (vaccineTypeRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
            throw new DuplicateResourceException("Another vaccine type already exists with code: " + normalizedCode);
        }

        vaccineTypeMapper.updateEntityFromDto(requestDto, vaccineType);
        vaccineType.setCode(normalizedCode);
        vaccineType.setName(requestDto.getName().trim());

        VaccineType updated = vaccineTypeRepository.saveAndFlush(vaccineType);
        log.info("Successfully updated vaccine type ID: {}", id);
        return vaccineTypeMapper.toResponseDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<VaccineTypeResponseDto> getAllVaccineTypes(Boolean mandatoryOnly, Pageable pageable) {
        Page<VaccineType> page = Boolean.TRUE.equals(mandatoryOnly)
                ? vaccineTypeRepository.findAllByIsMandatoryTrue(pageable)
                : vaccineTypeRepository.findAll(pageable);

        List<VaccineTypeResponseDto> items = vaccineTypeMapper.toResponseDtoList(page.getContent());
        return PagedResponseDto.of(items, PageMetaDataDto.from(page));
    }

    @Override
    @Transactional
    public void deleteVaccineType(Long id) {
        log.info("Deleting vaccine type ID: {}", id);

        VaccineType vaccineType = vaccineTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccine type not found with ID: " + id));

        vaccineTypeRepository.delete(vaccineType);
        log.info("Successfully deleted vaccine type ID: {}", id);
    }
}