package com.topschicken.employeeservice.service.impl;

import com.topschicken.employeeservice.dto.request.VaccinationRecordRequestDto;
import com.topschicken.employeeservice.dto.response.VaccinationRecordResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PageMetaDataDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.entity.Employee;
import com.topschicken.employeeservice.entity.VaccinationRecord;
import com.topschicken.employeeservice.entity.VaccineType;
import com.topschicken.employeeservice.entity.enums.VaccinationStatus;
import com.topschicken.employeeservice.exception.DuplicateResourceException;
import com.topschicken.employeeservice.exception.ResourceNotFoundException;
import com.topschicken.employeeservice.repository.EmployeeRepository;
import com.topschicken.employeeservice.repository.VaccinationRecordRepository;
import com.topschicken.employeeservice.repository.VaccineTypeRepository;
import com.topschicken.employeeservice.service.VaccinationRecordService;
import com.topschicken.employeeservice.util.mapper.VaccinationRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VaccinationRecordServiceImpl implements VaccinationRecordService {

    private final VaccinationRecordRepository vaccinationRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final VaccineTypeRepository vaccineTypeRepository;
    private final VaccinationRecordMapper vaccinationRecordMapper;

    @Override
    @Transactional
    public VaccinationRecordResponseDto createVaccinationRecord(VaccinationRecordRequestDto requestDto) {
        log.info("Creating vaccination record for employee ID: {}, vaccineType ID: {}, dose: {}",
                requestDto.getEmployeeId(), requestDto.getVaccineTypeId(), requestDto.getDoseNumber());

        if (vaccinationRecordRepository.existsByEmployeeIdAndVaccineTypeIdAndDoseNumber(
                requestDto.getEmployeeId(), requestDto.getVaccineTypeId(), requestDto.getDoseNumber())) {
            throw new DuplicateResourceException(String.format(
                    "Vaccination record already exists for employee ID %d, vaccine type ID %d, dose %d",
                    requestDto.getEmployeeId(), requestDto.getVaccineTypeId(), requestDto.getDoseNumber()));
        }

        Employee employee = employeeRepository.findById(requestDto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with ID: " + requestDto.getEmployeeId()));

        VaccineType vaccineType = vaccineTypeRepository.findById(requestDto.getVaccineTypeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Vaccine type not found with ID: " + requestDto.getVaccineTypeId()));

        LocalDate resolvedExpiryDate = resolveAndValidateExpiryDate(
                requestDto.getAdministeredDate(), requestDto.getExpiryDate(), vaccineType);

        VaccinationRecord record = vaccinationRecordMapper.toEntity(requestDto);
        record.setEmployee(employee);
        record.setVaccineType(vaccineType);
        record.setExpiryDate(resolvedExpiryDate);
        record.setStatus(requestDto.getStatus() != null ? requestDto.getStatus() : VaccinationStatus.VALID);

        VaccinationRecord saved = vaccinationRecordRepository.save(record);
        log.info("Successfully created vaccination record ID: {} for employee ID: {}", saved.getId(), employee.getId());
        return vaccinationRecordMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<VaccinationRecordResponseDto> getAllVaccinationRecords(Pageable pageable) {
        Page<VaccinationRecord> page = vaccinationRecordRepository.findAll(pageable);
        List<VaccinationRecordResponseDto> items = vaccinationRecordMapper.toResponseDtoList(page.getContent());
        return PagedResponseDto.of(items, PageMetaDataDto.from(page));
    }

    @Override
    @Transactional(readOnly = true)
    public VaccinationRecordResponseDto getVaccinationRecordById(Long id) {
        VaccinationRecord record = vaccinationRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination record not found with ID: " + id));
        return vaccinationRecordMapper.toResponseDto(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VaccinationRecordResponseDto> getVaccinationRecordsByEmployeeId(Long employeeId, Long vaccineTypeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }

        List<VaccinationRecord> records = (vaccineTypeId != null)
                ? vaccinationRecordRepository.findAllByEmployeeIdAndVaccineTypeId(employeeId, vaccineTypeId)
                : vaccinationRecordRepository.findAllByEmployeeIdWithVaccineType(employeeId);

        return vaccinationRecordMapper.toResponseDtoList(records);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VaccinationRecordResponseDto> getExpiringVaccinations(int days, VaccinationStatus status) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        VaccinationStatus targetStatus = (status != null) ? status : VaccinationStatus.VALID;

        return vaccinationRecordMapper.toResponseDtoList(
                vaccinationRecordRepository.findExpiringVaccinations(targetStatus, today, endDate));
    }

    @Override
    @Transactional
    public VaccinationRecordResponseDto updateVaccinationRecord(Long id, VaccinationRecordRequestDto requestDto) {
        log.info("Updating vaccination record ID: {}", id);

        VaccinationRecord record = vaccinationRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination record not found with ID: " + id));

        boolean keyChanged = !record.getEmployee().getId().equals(requestDto.getEmployeeId())
                || !record.getVaccineType().getId().equals(requestDto.getVaccineTypeId())
                || !record.getDoseNumber().equals(requestDto.getDoseNumber());

        if (keyChanged && vaccinationRecordRepository.existsByEmployeeIdAndVaccineTypeIdAndDoseNumber(
                requestDto.getEmployeeId(), requestDto.getVaccineTypeId(), requestDto.getDoseNumber())) {
            throw new DuplicateResourceException(String.format(
                    "Vaccination record already exists for employee ID %d, vaccine type ID %d, dose %d",
                    requestDto.getEmployeeId(), requestDto.getVaccineTypeId(), requestDto.getDoseNumber()));
        }

        if (!record.getEmployee().getId().equals(requestDto.getEmployeeId())) {
            Employee newEmployee = employeeRepository.findById(requestDto.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee not found with ID: " + requestDto.getEmployeeId()));
            record.setEmployee(newEmployee);
        }

        VaccineType vaccineType = record.getVaccineType();
        if (!vaccineType.getId().equals(requestDto.getVaccineTypeId())) {
            vaccineType = vaccineTypeRepository.findById(requestDto.getVaccineTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Vaccine type not found with ID: " + requestDto.getVaccineTypeId()));
            record.setVaccineType(vaccineType);
        }

        LocalDate resolvedExpiryDate = resolveAndValidateExpiryDate(
                requestDto.getAdministeredDate(), requestDto.getExpiryDate(), vaccineType);

        vaccinationRecordMapper.updateEntityFromDto(requestDto, record);
        record.setExpiryDate(resolvedExpiryDate);

        if (requestDto.getStatus() != null) {
            record.setStatus(requestDto.getStatus());
        }

        VaccinationRecord updated = vaccinationRecordRepository.saveAndFlush(record);
        log.info("Successfully updated vaccination record ID: {}", id);
        return vaccinationRecordMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteVaccinationRecord(Long id) {
        log.info("Deleting vaccination record ID: {}", id);

        VaccinationRecord record = vaccinationRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaccination record not found with ID: " + id));

        vaccinationRecordRepository.delete(record);
        log.info("Successfully deleted vaccination record ID: {}", id);
    }

    private LocalDate resolveAndValidateExpiryDate(
            LocalDate administeredDate, LocalDate requestedExpiryDate, VaccineType vaccineType) {
        if (requestedExpiryDate != null) {
            if (requestedExpiryDate.isBefore(administeredDate)) {
                throw new IllegalArgumentException("Expiry date cannot be earlier than administered date");
            }
            return requestedExpiryDate;
        }
        return (vaccineType.getValidityMonths() != null)
                ? administeredDate.plusMonths(vaccineType.getValidityMonths())
                : null;
    }
}