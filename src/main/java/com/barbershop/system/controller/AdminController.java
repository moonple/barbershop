package com.barbershop.system.controller;

import com.barbershop.system.entity.Admin;
import com.barbershop.system.repository.AdminRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    @Autowired
    private AdminRepository adminRepository;

    // 系统启动时，如果没有任何管理员，自动创建一个超级管理员
    // 账号：13800000000，密码：123456
    @PostConstruct
    public void initSuperAdmin() {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setName("超级管理员");
            admin.setPhone("13800000000");
            admin.setPassword("123456");
            admin.setGender("男");
            adminRepository.save(admin);
        }
    }

    // 1. 管理员登录接口
    @PostMapping("/login")
    public Admin login(@RequestBody Map<String, String> loginData) {
        String phone = loginData.get("phone");
        String password = loginData.get("password");

        // 1. 打印看看前端传来了什么
        System.out.println(">>> 正在尝试登录...");
        System.out.println(">>> 接收到的手机号: " + phone);
        System.out.println(">>> 接收到的密码: " + password);

        Admin admin = adminRepository.findByPhone(phone);

        // 2. 打印看看数据库查到了什么
        if (admin == null) {
            System.out.println(">>> 错误：数据库没查到该手机号！");
            throw new RuntimeException("账号不存在");
        } else {
            System.out.println(">>> 数据库查到用户: " + admin.getName() + ", 密码: " + admin.getPassword());
        }

        // 3. 校验密码
        if (admin.getPassword().equals(password)) {
            System.out.println(">>> 登录成功！");
            return admin;
        } else {
            System.out.println(">>> 错误：密码不匹配！");
            throw new RuntimeException("密码错误");
        }
    }

    // 2. 获取所有管理员列表
    @GetMapping
    public List<Admin> getAll() {
        return adminRepository.findAll();
    }

    // 3. 添加管理员
    // 3. 添加管理员（增强版校验）
    @PostMapping
    public Admin add(@RequestBody Admin admin) {
        // 1. 非空校验
        if (admin.getName() == null || admin.getName().trim().isEmpty()) {
            throw new RuntimeException("姓名不能为空");
        }
        if (admin.getPhone() == null || admin.getPhone().trim().isEmpty()) {
            throw new RuntimeException("手机号不能为空");
        }

        // 2. 手机号格式校验 (正则表达式: 1开头, 第二位3-9, 后面9位数字)
        if (!admin.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式不正确，请输入11位有效手机号");
        }

        // 3. 手机号唯一性校验 (防止添加重复账号)
        if (adminRepository.findByPhone(admin.getPhone()) != null) {
            throw new RuntimeException("该手机号已存在，请勿重复添加");
        }

        // 4. 设置默认值
        if (admin.getGender() == null) admin.setGender("男");
        if (admin.getPassword() == null) admin.setPassword("123456"); // 默认密码

        return adminRepository.save(admin);
    }

    // 4. 修改管理员信息
    // 4. 修改管理员信息
    @PutMapping
    public Admin update(@RequestBody Admin admin) {
        // 1. 检查 ID 是否存在
        Admin old = adminRepository.findById(admin.getId())
                .orElseThrow(() -> new RuntimeException("管理员不存在"));

        // 2. 局部更新：只有前端传了值才修改
        if(admin.getName() != null && !admin.getName().isEmpty()) {
            old.setName(admin.getName());
        }
        if(admin.getGender() != null && !admin.getGender().isEmpty()) {
            old.setGender(admin.getGender());
        }
        if(admin.getPhone() != null && !admin.getPhone().isEmpty()) {
            // 这里可以加一个后端校验，防止手机号改重复了
            // (为了简单，这里暂时不写查重逻辑，只更新)
            old.setPhone(admin.getPhone());
        }

        return adminRepository.save(old);
    }

    // 5. 删除管理员 (带防自删校验)
    // URL参数 need: /api/admins/5?currentAdminId=1
    @DeleteMapping("/{targetId}")
    public void delete(@PathVariable Integer targetId, @RequestParam Integer currentAdminId) {
        if (targetId.equals(currentAdminId)) {
            throw new RuntimeException("操作失败：您不能删除当前登录的账号！");
        }
        adminRepository.deleteById(targetId);
    }

    // 管理员搜索接口
    @GetMapping("/search")
    public List<Admin> search(@RequestParam String keyword) {
        // 如果关键字为空，返回所有；否则进行模糊查询
        if (keyword == null || keyword.trim().isEmpty()) {
            return adminRepository.findAll();
        }
        return adminRepository.findByPhoneContaining(keyword);
    }
}