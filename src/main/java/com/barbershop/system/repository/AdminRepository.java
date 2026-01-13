package com.barbershop.system.repository;

import com.barbershop.system.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // 记得导入List

public interface AdminRepository extends JpaRepository<Admin, Integer> {
    // 登录
    Admin findByPhone(String phone);

    // 搜索用 (模糊匹配)
    List<Admin> findByPhoneContaining(String phone);
}