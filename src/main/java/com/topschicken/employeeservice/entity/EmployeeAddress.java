package com.topschicken.employeeservice.entity;

import com.topschicken.employeeservice.entity.enums.AddressType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "employee_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeAddress extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "address_type", nullable = false, length = 20)
    @Builder.Default
    private AddressType addressType = AddressType.PERMANENT;

    @Column(name = "line_1", nullable = false, length = 150)
    private String line1;

    @Column(name = "line_2", length = 150)
    private String line2;

    @Column(name = "street", length = 100)
    private String street;

    @Column(name = "city", nullable = false, length = 100)
    private String city;

    @Column(name = "postal_code", length = 20)
    private String postalCode;
}