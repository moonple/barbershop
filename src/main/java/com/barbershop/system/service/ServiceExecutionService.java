package com.barbershop.system.service;

import com.barbershop.system.entity.ServiceExecution;
import com.barbershop.system.entity.Member;
import com.barbershop.system.entity.ServiceItem;
import com.barbershop.system.entity.Employee;
import com.barbershop.system.repository.ServiceExecutionRepository;
import com.barbershop.system.repository.MemberRepository;
import com.barbershop.system.repository.ServiceItemRepository;
import com.barbershop.system.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 服务执行管理服务类
 */
@Service
public class ServiceExecutionService {

    @Autowired
    private ServiceExecutionRepository serviceExecutionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private MemberService memberService;

    /**
     * 查询所有服务执行记录
     */
    public List<ServiceExecution> findAll() {
        return serviceExecutionRepository.findAll();
    }

    /**
     * 根据条件查询（支持单个或组合查询）
     */
    public List<ServiceExecution> findByFilters(Integer memberId, Integer serviceId, Integer employeeId, LocalDate serviceDate) {
        return serviceExecutionRepository.findByFilters(memberId, serviceId, employeeId, serviceDate);
    }

    /**
     * 根据ID查询单条记录
     */
    public Optional<ServiceExecution> findById(Integer id) {
        return serviceExecutionRepository.findById(id);
    }

    /**
     * 新增服务执行记录
     */
    @Transactional
    public ServiceExecution add(Integer memberId, Integer serviceId, Integer employeeId, LocalDate serviceDate) {
        // 验证会员是否存在
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("会员不存在"));

        // 验证服务项目是否存在
        ServiceItem serviceItem = serviceItemRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("服务项目不存在"));

        // 验证员工是否存在
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("员工不存在"));

        // 创建服务执行记录
        ServiceExecution execution = new ServiceExecution();
        execution.setMemberId(memberId);
        execution.setMemberName(member.getName());
        execution.setServiceId(serviceId);
        execution.setServiceName(serviceItem.getName());
        execution.setEmployeeId(employeeId);
        execution.setEmployeeName(employee.getName());
        execution.setCost(serviceItem.getPrice());
        execution.setServiceDate(serviceDate != null ? serviceDate : LocalDate.now());

        // 保存执行记录
        ServiceExecution savedExecution = serviceExecutionRepository.save(execution);

        // 扣除会员余额
        memberService.deductBalance(memberId, serviceItem.getPrice());

        return savedExecution;
    }

    /**
     * 修改服务执行记录
     */
    @Transactional
    public ServiceExecution update(Integer id, Integer memberId, Integer serviceId, Integer employeeId, LocalDate serviceDate) {
        ServiceExecution execution = serviceExecutionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("服务执行记录不存在"));

        // 如果更新了会员、服务或员工，需要重新获取相关信息
        if (memberId != null && !memberId.equals(execution.getMemberId())) {
            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new RuntimeException("会员不存在"));
            execution.setMemberId(memberId);
            execution.setMemberName(member.getName());
        }

        if (serviceId != null && !serviceId.equals(execution.getServiceId())) {
            ServiceItem serviceItem = serviceItemRepository.findById(serviceId)
                    .orElseThrow(() -> new RuntimeException("服务项目不存在"));
            execution.setServiceId(serviceId);
            execution.setServiceName(serviceItem.getName());
            execution.setCost(serviceItem.getPrice());
        }

        if (employeeId != null && !employeeId.equals(execution.getEmployeeId())) {
            Employee employee = employeeRepository.findById(employeeId)
                    .orElseThrow(() -> new RuntimeException("员工不存在"));
            execution.setEmployeeId(employeeId);
            execution.setEmployeeName(employee.getName());
        }

        if (serviceDate != null) {
            execution.setServiceDate(serviceDate);
        }

        return serviceExecutionRepository.save(execution);
    }

    /**
     * 删除服务执行记录
     */
    @Transactional
    public void delete(Integer id) {
        serviceExecutionRepository.deleteById(id);
    }

    /**
     * 从预约记录创建服务执行记录（带幂等性控制）
     * 如果该预约已经创建过执行记录，则不重复创建
     */
    @Transactional
    public ServiceExecution createFromAppointment(Integer appointmentId, Integer memberId, String memberName,
                                                   Integer serviceId, String serviceName,
                                                   Integer employeeId, String employeeName,
                                                   LocalDate serviceDate) {
        // 检查是否已存在该预约的执行记录（幂等性控制）
        Optional<ServiceExecution> existing = serviceExecutionRepository.findByAppointmentId(appointmentId);
        if (existing.isPresent()) {
            return existing.get(); // 已存在，直接返回，不重复扣费
        }

        // 获取服务项目费用
        ServiceItem serviceItem = serviceItemRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("服务项目不存在"));

        // 创建新的服务执行记录
        ServiceExecution execution = new ServiceExecution();
        execution.setAppointmentId(appointmentId);
        execution.setMemberId(memberId);
        execution.setMemberName(memberName);
        execution.setServiceId(serviceId);
        execution.setServiceName(serviceName);
        execution.setEmployeeId(employeeId);
        execution.setEmployeeName(employeeName);
        execution.setCost(serviceItem.getPrice());
        execution.setServiceDate(serviceDate != null ? serviceDate : LocalDate.now());

        // Save execution record
        ServiceExecution savedExecution = serviceExecutionRepository.save(execution);

        // Deduct member balance (only when creating new record, not on duplicate)
        memberService.deductBalance(memberId, serviceItem.getPrice());

        return savedExecution;
    }
}
