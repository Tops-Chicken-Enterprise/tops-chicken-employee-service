package com.topschicken.employeeservice.service;

import com.topschicken.employeeservice.dto.request.BiosecurityEntryLogRequestDto;
import com.topschicken.employeeservice.dto.response.BiosecurityEntryLogResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.entity.enums.SanitizationStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface BiosecurityEntryLogService {

    BiosecurityEntryLogResponseDto createEntryLog(BiosecurityEntryLogRequestDto requestDto);

    PagedResponseDto<BiosecurityEntryLogResponseDto> getAllEntryLogs(Pageable pageable);

    BiosecurityEntryLogResponseDto getEntryLogById(Long id);

    List<BiosecurityEntryLogResponseDto> getEntryLogsByEmployeeId(Long employeeId);

    PagedResponseDto<BiosecurityEntryLogResponseDto> getLogsByFarmAndDateRange(
            Long farmId, Long shedId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    List<BiosecurityEntryLogResponseDto> getRecentIncidents(SanitizationStatus status, int hours);

    BiosecurityEntryLogResponseDto recordExitTime(Long id, LocalDateTime exitTime);

    BiosecurityEntryLogResponseDto updateEntryLog(Long id, BiosecurityEntryLogRequestDto requestDto);

    void deleteEntryLog(Long id);
}