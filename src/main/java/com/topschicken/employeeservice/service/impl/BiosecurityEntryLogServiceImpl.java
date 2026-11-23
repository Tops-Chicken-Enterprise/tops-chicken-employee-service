package com.topschicken.employeeservice.service.impl;

import com.topschicken.employeeservice.dto.request.BiosecurityEntryLogRequestDto;
import com.topschicken.employeeservice.dto.response.BiosecurityEntryLogResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PageMetaDataDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.entity.BiosecurityEntryLog;
import com.topschicken.employeeservice.entity.Employee;
import com.topschicken.employeeservice.entity.enums.SanitizationStatus;
import com.topschicken.employeeservice.exception.ResourceNotFoundException;
import com.topschicken.employeeservice.repository.BiosecurityEntryLogRepository;
import com.topschicken.employeeservice.repository.EmployeeRepository;
import com.topschicken.employeeservice.service.BiosecurityEntryLogService;
import com.topschicken.employeeservice.util.mapper.BiosecurityEntryLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BiosecurityEntryLogServiceImpl implements BiosecurityEntryLogService {

    private final BiosecurityEntryLogRepository entryLogRepository;
    private final EmployeeRepository employeeRepository;
    private final BiosecurityEntryLogMapper entryLogMapper;

    @Override
    @Transactional
    public BiosecurityEntryLogResponseDto createEntryLog(BiosecurityEntryLogRequestDto requestDto) {
        log.info("Creating biosecurity entry log for employee ID: {}, farm ID: {}, shed ID: {}",
                requestDto.getEmployeeId(), requestDto.getFarmId(), requestDto.getShedId());

        validateTimes(requestDto.getEntryTime(), requestDto.getExitTime());

        Employee employee = employeeRepository.findById(requestDto.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee not found with ID: " + requestDto.getEmployeeId()));

        BiosecurityEntryLog logEntry = entryLogMapper.toEntity(requestDto);
        logEntry.setEmployee(employee);

        if (requestDto.getClearedById() != null) {
            Employee clearer = employeeRepository.findById(requestDto.getClearedById())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Clearing officer employee not found with ID: " + requestDto.getClearedById()));
            logEntry.setClearedBy(clearer);
        }

        BiosecurityEntryLog saved = entryLogRepository.save(logEntry);
        log.info("Successfully created biosecurity entry log ID: {}", saved.getId());
        return entryLogMapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<BiosecurityEntryLogResponseDto> getAllEntryLogs(Pageable pageable) {
        Page<BiosecurityEntryLog> page = entryLogRepository.findAll(pageable);
        List<BiosecurityEntryLogResponseDto> items = entryLogMapper.toResponseDtoList(page.getContent());
        return PagedResponseDto.of(items, PageMetaDataDto.from(page));
    }

    @Override
    @Transactional(readOnly = true)
    public BiosecurityEntryLogResponseDto getEntryLogById(Long id) {
        BiosecurityEntryLog logEntry = entryLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Biosecurity entry log not found with ID: " + id));
        return entryLogMapper.toResponseDto(logEntry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BiosecurityEntryLogResponseDto> getEntryLogsByEmployeeId(Long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee not found with ID: " + employeeId);
        }
        return entryLogMapper.toResponseDtoList(
                entryLogRepository.findAllByEmployeeIdOrderByEntryTimeDesc(employeeId));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<BiosecurityEntryLogResponseDto> getLogsByFarmAndDateRange(
            Long farmId, Long shedId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be earlier than start time");
        }
        Page<BiosecurityEntryLog> page = entryLogRepository.findLogsByFarmAndDateRange(
                farmId, shedId, startTime, endTime, pageable);
        List<BiosecurityEntryLogResponseDto> items = entryLogMapper.toResponseDtoList(page.getContent());
        return PagedResponseDto.of(items, PageMetaDataDto.from(page));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BiosecurityEntryLogResponseDto> getRecentIncidents(SanitizationStatus status, int hours) {
        SanitizationStatus targetStatus = (status != null) ? status : SanitizationStatus.VIOLATION;
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return entryLogMapper.toResponseDtoList(entryLogRepository.findRecentIncidents(targetStatus, since));
    }

    @Override
    @Transactional
    public BiosecurityEntryLogResponseDto recordExitTime(Long id, LocalDateTime exitTime) {
        log.info("Recording exit time for biosecurity log ID: {}", id);

        BiosecurityEntryLog logEntry = entryLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Biosecurity entry log not found with ID: " + id));

        LocalDateTime resolvedExitTime = (exitTime != null) ? exitTime : LocalDateTime.now();
        validateTimes(logEntry.getEntryTime(), resolvedExitTime);

        logEntry.setExitTime(resolvedExitTime);
        BiosecurityEntryLog updated = entryLogRepository.saveAndFlush(logEntry);
        return entryLogMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public BiosecurityEntryLogResponseDto updateEntryLog(Long id, BiosecurityEntryLogRequestDto requestDto) {
        log.info("Updating biosecurity entry log ID: {}", id);

        validateTimes(requestDto.getEntryTime(), requestDto.getExitTime());

        BiosecurityEntryLog logEntry = entryLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Biosecurity entry log not found with ID: " + id));

        if (!logEntry.getEmployee().getId().equals(requestDto.getEmployeeId())) {
            Employee newEmployee = employeeRepository.findById(requestDto.getEmployeeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Employee not found with ID: " + requestDto.getEmployeeId()));
            logEntry.setEmployee(newEmployee);
        }

        entryLogMapper.updateEntityFromDto(requestDto, logEntry);

        if (requestDto.getClearedById() != null) {
            Employee clearer = employeeRepository.findById(requestDto.getClearedById())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Clearing officer employee not found with ID: " + requestDto.getClearedById()));
            logEntry.setClearedBy(clearer);
        }

        BiosecurityEntryLog updated = entryLogRepository.saveAndFlush(logEntry);
        log.info("Successfully updated biosecurity entry log ID: {}", id);
        return entryLogMapper.toResponseDto(updated);
    }

    @Override
    @Transactional
    public void deleteEntryLog(Long id) {
        log.info("Deleting biosecurity entry log ID: {}", id);

        BiosecurityEntryLog logEntry = entryLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Biosecurity entry log not found with ID: " + id));

        entryLogRepository.delete(logEntry);
        log.info("Successfully deleted biosecurity entry log ID: {}", id);
    }

    private void validateTimes(LocalDateTime entryTime, LocalDateTime exitTime) {
        if (exitTime != null && exitTime.isBefore(entryTime)) {
            throw new IllegalArgumentException("Exit time cannot be earlier than entry time");
        }
    }
}