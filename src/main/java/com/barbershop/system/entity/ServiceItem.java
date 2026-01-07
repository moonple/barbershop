package com.barbershop.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Data
public class ServiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private BigDecimal price;
    private Integer duration;      // 服务用时
    private String consumeItemIds; // 消耗品ID字符串
}