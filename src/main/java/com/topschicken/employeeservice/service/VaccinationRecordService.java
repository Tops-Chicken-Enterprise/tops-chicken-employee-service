package com.topschicken.employeeservice.service;

import com.topschicken.employeeservice.dto.request.VaccinationRecordRequestDto;
import com.topschicken.employeeservice.dto.response.VaccinationRecordResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.entity.enums.VaccinationStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VaccinationRecordService {

    VaccinationRecordResponseDto createVaccinationRecord(VaccinationRecordRequestDto requestDto);

    PagedResponseDto<VaccinationRecordResponseDto> getAllVaccinationRecords(Pageable pageable);

    VaccinationRecordResponseDto getVaccinationRecordById(Long id);

    List<VaccinationRecordResponseDto> getVaccinationRecordsByEmployeeId(Long employeeId, Long vaccineTypeId);

    List<VaccinationRecordResponseDto> getExpiringVaccinations(int days, VaccinationStatus status);

    VaccinationRecordResponseDto updateVaccinationRecord(Long id, VaccinationRecordRequestDto requestDto);

    void deleteVaccinationRecord(Long id);
}