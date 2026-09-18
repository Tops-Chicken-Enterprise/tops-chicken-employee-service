package com.topschicken.employeeservice.entity;

import com.topschicken.employeeservice.entity.enums.SanitizationStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "biosecurity_entry_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BiosecurityEntryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "farm_id", nullable = false)
    private Long farmId;

    @Column(name = "shed_id", nullable = false)
    private Long shedId;

    @Column(name = "entry_time", nullable = false)
    private LocalDateTime entryTime;

    @Column(name = "exit_time")
    private LocalDateTime exitTime;

    @Column(name = "shower_completed", nullable = false)
    @Builder.Default
    private Boolean showerCompleted = false;

    @Column(name = "clothing_changed", nullable = false)
    @Builder.Default
    private Boolean clothingChanged = false;

    @Column(name = "body_temperature", precision = 4, scale = 1)
    private BigDecimal bodyTemperature;

    @Enumerated(EnumType.STRING)
    @Column(name = "sanitization_status", nullable = false, length = 20)
    @Builder.Default
    private SanitizationStatus sanitizationStatus = SanitizationStatus.PASSED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cleared_by")
    private Employee clearedBy;

    @Column(name = "remarks", length = 255)
    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}