package com.topschicken.employeeservice.dto.response;

import com.topschicken.employeeservice.entity.enums.EmploymentStatus;
import com.topschicken.employeeservice.entity.enums.EmploymentType;
import com.topschicken.employeeservice.entity.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeResponseDto {

    private Long id;
    private String empCode;
    private String firstName;
    private String middleName;
    private String lastName;
    private String fullName;
    private String nic;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String email;
    private String contactNumber;
    private String emergencyContact;
    private String epfNumber;
    private EmploymentType employmentType;
    private EmploymentStatus employmentStatus;
    private LocalDate joinedDate;
    private LocalDate resignedDate;

    private Integer biometricId;
    private Long farmId;
    private Long shedId;


    private Long designationId;
    private String designationTitle;
    private Long departmentId;
    private String departmentName;


    private Long supervisorId;
    private String supervisorName;

    private String wso2UserId;
    private String profileImageUrl;


    private List<EmployeeAddressResponseDto> addresses;
    private List<EmployeeBankAccountResponseDto> bankAccounts;
    private List<EmployeeDocumentResponseDto> documents;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}