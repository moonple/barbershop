package com.barbershop.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;


@Entity
@Data
public class ServiceExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 会员信息
    private Integer memberId;
    private String memberName;

    // 服务项目信息
    private Integer serviceId;
    private String serviceName;

    // 员工信息
    private Integer employeeId;
    private String employeeName;

    // 费用（从服务项目中获取）
    private BigDecimal cost;

    // 备注/日期（编辑时可选择，列表展示为当天日期）
    private LocalDate serviceDate;

    // 关联的预约ID（用于幂等性控制，避免重复插入）
    @Column(unique = true)
    private Integer appointmentId;
}
