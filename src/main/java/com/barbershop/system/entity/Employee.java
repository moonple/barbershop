package com.barbershop.system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // 工号，自动分配

    private String name;
    private String phone;
    private String gender;
}