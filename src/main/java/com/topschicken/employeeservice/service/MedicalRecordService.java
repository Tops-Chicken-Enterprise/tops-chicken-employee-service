package com.topschicken.employeeservice.service;

import com.topschicken.employeeservice.dto.request.MedicalRecordRequestDto;
import com.topschicken.employeeservice.dto.response.MedicalRecordResponseDto;
import com.topschicken.employeeservice.dto.response.paginate.PagedResponseDto;
import com.topschicken.employeeservice.entity.enums.FitnessStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MedicalRecordService {

    MedicalRecordResponseDto createMedicalRecord(MedicalRecordRequestDto requestDto);

    PagedResponseDto<MedicalRecordResponseDto> getAllMedicalRecords(Pageable pageable);

    MedicalRecordResponseDto getMedicalRecordById(Long id);

    List<MedicalRecordResponseDto> getMedicalRecordsByEmployeeId(Long employeeId, FitnessStatus fitnessStatus);

    MedicalRecordResponseDto getLatestMedicalRecordByEmployeeId(Long employeeId);

    List<MedicalRecordResponseDto> getExpiringMedicalRecords(int days);

    MedicalRecordResponseDto updateMedicalRecord(Long id, MedicalRecordRequestDto requestDto);

    void deleteMedicalRecord(Long id);
}