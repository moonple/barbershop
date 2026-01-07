package com.barbershop.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer memberId;
    private String memberName;

    private Integer serviceId;
    private String serviceName;

    private Integer employeeId;
    private String employeeName;

    // ↓↓↓ 新增字段，修复 setAppointmentDate 报错 ↓↓↓
    private LocalDate appointmentDate;
    private String timeRange;
    // ↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private String status;
    private String remark;
}