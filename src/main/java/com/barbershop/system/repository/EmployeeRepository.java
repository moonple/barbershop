package com.barbershop.system.repository;

import com.barbershop.system.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    // 搜索用：根据手机号查询
    List<Employee> findByPhone(String phone);

}