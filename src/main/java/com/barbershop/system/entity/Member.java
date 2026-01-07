package com.barbershop.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

// @Entity 告诉 Spring Boot 这是一个数据库实体
// @Data 是 Lombok 插件，自动帮你生成 get/set 方法
@Entity
@Data
public class Member {

    // @Id 表示这是主键
    // @GeneratedValue 表示主键自动增长 (Auto Increment)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 会员姓名
    private String name;

    // 手机号，设为唯一
    @Column(unique = true)
    private String phone;

    // 性别
    private String gender;

    // 余额 (用 BigDecimal 存钱更精确)
    private BigDecimal balance;

    // 注册时间
    private LocalDateTime registerTime;

    // 在数据存入数据库之前，自动记录当前时间
    @PrePersist
    public void prePersist() {
        if (registerTime == null) {
            registerTime = LocalDateTime.now();
        }
        if (balance == null) {
            balance = BigDecimal.ZERO;
        }
    }
}