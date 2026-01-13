package com.barbershop.system.controller;

import com.barbershop.system.entity.Employee;
import com.barbershop.system.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping
    public List<Employee> getAll() {
        return employeeRepository.findAll();
    }

    @PostMapping
    public Employee add(@RequestBody Employee emp) {
        // trim().isEmpty()
        if (emp.getName() == null || emp.getName().trim().isEmpty()) {
            throw new RuntimeException("员工姓名不能为空");
        }
        if (emp.getPhone() == null || emp.getPhone().trim().isEmpty()) {
            throw new RuntimeException("员工手机号不能为空");
        }

        // 2. 格式校验
        if (!emp.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式错误");
        }

        if (!employeeRepository.findByPhone(emp.getPhone()).isEmpty()) {
            throw new RuntimeException("该手机号已绑定其他员工");
        }

        return employeeRepository.save(emp);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        employeeRepository.deleteById(id);
    }

    // 搜索接口
    @GetMapping("/search")
    public List<Employee> search(@RequestParam String keyword) {
        if (keyword.matches("\\d+")) {
            return employeeRepository.findById(Integer.valueOf(keyword))
                    .map(List::of)
                    .orElseGet(() -> employeeRepository.findByPhone(keyword));
        } else {
            return employeeRepository.findByPhone(keyword);
        }
    }



    @PutMapping("/{id}")
    public Employee update(@PathVariable Integer id, @RequestBody Employee newEmp) {
        Employee oldEmp = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("员工不存在"));


        if (newEmp.getName() != null && !newEmp.getName().isEmpty()) {
            oldEmp.setName(newEmp.getName());
        }
        if (newEmp.getPhone() != null && !newEmp.getPhone().isEmpty()) {

            oldEmp.setPhone(newEmp.getPhone());
        }
        if (newEmp.getGender() != null && !newEmp.getGender().isEmpty()) {
            oldEmp.setGender(newEmp.getGender());
        }

        return employeeRepository.save(oldEmp);
    }
}