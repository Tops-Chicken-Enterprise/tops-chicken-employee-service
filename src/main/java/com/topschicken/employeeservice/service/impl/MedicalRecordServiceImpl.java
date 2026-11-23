package com.topschicken.employeeservice.service.impl;

import com.topschicken.employeeservice.dto.request.MedicalRecordRequestDto;
import com.topschicken.employeeservice.dto.response.MedicalRecordResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PageMetaDataDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.entity.Employee;
import com.topschicken.employeeservice.entity.MedicalRecord;
import com.topschicken.employeeservice.entity.enums.FitnessStatus;
import com.topschicken.employeeservice.exception.ResourceNotFoundException;
import com.topschicken.employeeservice.repository.EmployeeRepository;
import com.topschicken.employeeservice.repository.MedicalRecordRepository;
import com.topschicken.employeeservice.service.MedicalRecordService;
import com.topschicken.employeeservice.util.mapper.MedicalRecordMapper;
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
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final MedicalRecordMapper medicalRecordMapper;

    @Override
    @Transactional
    public MedicalRecordResponseDto createMedicalRecord(MedicalRecordRequestDto requestDto) {
        log.info("Creating {} medical record for employee ID: {}", requestDto.getRecordType(), requestDto.getEmployeeId());

        validateDates(requestDto.getCheckupDate(), requestDto.getExpiryDate());

        Employee employee = employeeRepository.findById(requestDto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with ID: " + requestDto.getEmployeeId()));

        MedicalRecord medicalRecord = medicalRecordMapper.toEntity(requestDto);
        medicalRecord.setEmployee(employee);

        if (requestDto.getVerifiedById() != null) {
            Employee verifier = employeeRepository.findById(requestDto.getVerifiedById())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Verifier employee not found with ID: " + requestDto.getVerifiedById()));
            medicalRecord.setVerifiedBy(verifier);
        }

        MedicalRecord saved = medicalRecordRepository.save(medicalRecord);
        log.info("Successfully created medical record ID: {} for employee ID: {}", saved.getId(), employee.getId());
        return medicalRecordMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<MedicalRecordResponseDto> getAllMedicalRecords(Pageable pageable) {
        Page<MedicalRecord> page = medicalRecordRepository.findAll(pageable);
        List<MedicalRecordResponseDto> items = medicalRecordMapper.toResponseDtoList(page.getContent());
        return PagedResponseDto.of(items, PageMetaDataDto.from(page));
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordResponseDto getMedicalRecordById(Long id) {
        MedicalRecord medicalRecord = medicalRecordRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with ID: " + id));
        return medicalRecordMapper.toResponseDto(medicalRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponseDto> getMedicalRecordsByEmployeeId(Long employeeId, FitnessStatus fitnessStatus) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }

        List<MedicalRecord> records = (fitnessStatus != null)
                ? medicalRecordRepository.findAllByEmployeeIdAndFitnessStatus(employeeId, fitnessStatus)
                : medicalRecordRepository.findAllByEmployeeIdOrderByCheckupDateDesc(employeeId);

        return medicalRecordMapper.toResponseDtoList(records);
    }

    @Override
    @Transactional(readOnly = true)
    public MedicalRecordResponseDto getLatestMedicalRecordByEmployeeId(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }

        MedicalRecord latest = medicalRecordRepository.findFirstByEmployeeIdOrderByCheckupDateDesc(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No medical records found for employee ID: " + employeeId));

        return medicalRecordMapper.toResponseDto(latest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MedicalRecordResponseDto> getExpiringMedicalRecords(int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        return medicalRecordMapper.toResponseDtoList(
                medicalRecordRepository.findExpiringMedicalRecords(today, endDate));
    }

    @Override
    @Transactional
    public MedicalRecordResponseDto updateMedicalRecord(Long id, MedicalRecordRequestDto requestDto) {
        log.info("Updating medical record ID: {}", id);

        validateDates(requestDto.getCheckupDate(), requestDto.getExpiryDate());

        MedicalRecord medicalRecord = medicalRecordRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with ID: " + id));

        if (!medicalRecord.getEmployee().getId().equals(requestDto.getEmployeeId())) {
            Employee newEmployee = employeeRepository.findById(requestDto.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee not found with ID: " + requestDto.getEmployeeId()));
            medicalRecord.setEmployee(newEmployee);
        }

        medicalRecordMapper.updateEntityFromDto(requestDto, medicalRecord);

        if (requestDto.getVerifiedById() != null) {
            Employee verifier = employeeRepository.findById(requestDto.getVerifiedById())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Verifier employee not found with ID: " + requestDto.getVerifiedById()));
            medicalRecord.setVerifiedBy(verifier);
        }

        MedicalRecord updated = medicalRecordRepository.saveAndFlush(medicalRecord);
        log.info("Successfully updated medical record ID: {}", id);
        return medicalRecordMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteMedicalRecord(Long id) {
        log.info("Deleting medical record ID: {}", id);

        MedicalRecord medicalRecord = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medical record not found with ID: " + id));

        medicalRecordRepository.delete(medicalRecord);
        log.info("Successfully deleted medical record ID: {}", id);
    }

    private void validateDates(LocalDate checkupDate, LocalDate expiryDate) {
        if (expiryDate != null && expiryDate.isBefore(checkupDate)) {
            throw new IllegalArgumentException("Expiry date cannot be earlier than checkup date");
        }
    }
}