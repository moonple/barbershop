package com.barbershop.system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Admin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    // 手机号作为登录账号，必须唯一
    @Column(unique = true)
    private String phone;

    private String gender;

    private String password; // 登录密码
}