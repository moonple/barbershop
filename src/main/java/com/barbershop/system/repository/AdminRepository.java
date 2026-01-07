package com.barbershop.system.repository;

import com.barbershop.system.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List; // 记得导入List

public interface AdminRepository extends JpaRepository<Admin, Integer> {
    // 登录用 (精准匹配)
    Admin findByPhone(String phone);

    // 搜索用 (模糊匹配：只要包含这个数字就算)
    List<Admin> findByPhoneContaining(String phone);
}