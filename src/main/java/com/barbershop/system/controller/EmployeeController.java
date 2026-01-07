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

    // 添加员工 (带校验)
    @PostMapping
    public Employee add(@RequestBody Employee emp) {
        // 1. 非空校验 (注意：要用 trim().isEmpty() 来防空格)
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

        // 3. 查重校验 (可选，防止添加重复员工)
        if (!employeeRepository.findByPhone(emp.getPhone()).isEmpty()) {
            throw new RuntimeException("该手机号已绑定其他员工");
        }

        return employeeRepository.save(emp);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        employeeRepository.deleteById(id);
    }

    // 搜索接口：支持按工号搜索 或 按手机号搜索
    @GetMapping("/search")
    public List<Employee> search(@RequestParam String keyword) {
        // 1. 尝试看看是不是数字，如果是数字可能是查ID
        if (keyword.matches("\\d+")) {
            // 先按ID查，如果ID没有，再按手机号查
            return employeeRepository.findById(Integer.valueOf(keyword))
                    .map(List::of) // 这里的 List::of 需要 JDK9+，如果报错改成 Collections.singletonList
                    .orElseGet(() -> employeeRepository.findByPhone(keyword));
        } else {
            // 否则按手机号查
            return employeeRepository.findByPhone(keyword);
        }
    }


    // [新增] 修改员工信息接口
    @PutMapping("/{id}")
    public Employee update(@PathVariable Integer id, @RequestBody Employee newEmp) {
        Employee oldEmp = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("员工不存在"));

        // 局部更新逻辑
        if (newEmp.getName() != null && !newEmp.getName().isEmpty()) {
            oldEmp.setName(newEmp.getName());
        }
        if (newEmp.getPhone() != null && !newEmp.getPhone().isEmpty()) {
            // 这里也可以加查重逻辑，暂时省略
            oldEmp.setPhone(newEmp.getPhone());
        }
        if (newEmp.getGender() != null && !newEmp.getGender().isEmpty()) {
            oldEmp.setGender(newEmp.getGender());
        }

        return employeeRepository.save(oldEmp);
    }
}