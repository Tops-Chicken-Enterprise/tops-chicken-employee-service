package com.topschicken.employeeservice.dto.request;

import com.topschicken.employeeservice.entity.enums.EmploymentStatus;
import com.topschicken.employeeservice.entity.enums.EmploymentType;
import com.topschicken.employeeservice.entity.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeRequestDto {

    @NotBlank(message = "Employee code is mandatory")
    @Size(max = 20, message = "Employee code must not exceed 20 characters")
    private String empCode;

    @NotBlank(message = "First name is mandatory")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @Size(max = 50, message = "Middle name must not exceed 50 characters")
    private String middleName;

    @NotBlank(message = "Last name is mandatory")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "NIC is mandatory")
    @Pattern(regexp = "^([0-9]{9}[x|X|v|V]|[0-9]{12})$", message = "Invalid Sri Lankan NIC format (e.g., 200012345678 or 981234567V)")
    private String nic;

    @NotNull(message = "Gender is mandatory")
    private Gender gender;

    @NotNull(message = "Date of birth is mandatory")
    @Past(message = "Date of birth must be a past date")
    private LocalDate dateOfBirth;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Contact number is mandatory")
    @Size(max = 20, message = "Contact number must not exceed 20 characters")
    private String contactNumber;

    @NotBlank(message = "Emergency contact number is mandatory")
    @Size(max = 20, message = "Emergency contact must not exceed 20 characters")
    private String emergencyContact;

    @Size(max = 30, message = "EPF number must not exceed 30 characters")
    private String epfNumber;

    @NotNull(message = "Employment type is mandatory")
    private EmploymentType employmentType;

    @NotNull(message = "Employment status is mandatory")
    private EmploymentStatus employmentStatus;

    @NotNull(message = "Joined date is mandatory")
    private LocalDate joinedDate;

    private LocalDate resignedDate;


    private Integer biometricId;


    private Long farmId;
    private Long shedId;

    @NotNull(message = "Designation ID is mandatory")
    private Long designationId;

    private Long supervisorId;

    @Size(max = 100, message = "WSO2 User ID must not exceed 100 characters")
    private String wso2UserId;

    @Size(max = 255, message = "Profile image URL must not exceed 255 characters")
    private String profileImageUrl;

    @Valid
    @Builder.Default
    private List<EmployeeAddressRequestDto> addresses = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<EmployeeBankAccountRequestDto> bankAccounts = new ArrayList<>();
}