package com.barbershop.system.service;

import com.barbershop.system.entity.Member;
import com.barbershop.system.entity.ServiceExecution;
import com.barbershop.system.entity.ServiceItem;
import com.barbershop.system.entity.Employee;
import com.barbershop.system.repository.MemberRepository;
import com.barbershop.system.repository.ServiceExecutionRepository;
import com.barbershop.system.repository.ServiceItemRepository;
import com.barbershop.system.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberBalanceServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private ServiceExecutionService serviceExecutionService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ServiceExecutionRepository serviceExecutionRepository;

    private Member testMember;
    private ServiceItem testService;
    private Employee testEmployee;

    @BeforeEach
    void setUp() {
        // Create test member
        testMember = new Member();
        testMember.setName("测试会员");
        testMember.setPhone("13800138000");
        testMember.setGender("男");
        testMember.setBalance(BigDecimal.valueOf(100.00));
        testMember = memberRepository.save(testMember);

        // Create test service
        testService = new ServiceItem();
        testService.setName("测试服务");
        testService.setPrice(BigDecimal.valueOf(50.00));
        testService.setDuration(60);
        testService = serviceItemRepository.save(testService);

        // Create test employee
        testEmployee = new Employee();
        testEmployee.setName("测试员工");
        testEmployee.setPhone("13900139000");
        testEmployee.setGender("女");
        testEmployee = employeeRepository.save(testEmployee);
    }

    @Test
    void testSimpleRecharge() {
        // Test simple recharge (without financial record)
        BigDecimal initialBalance = testMember.getBalance();
        BigDecimal rechargeAmount = BigDecimal.valueOf(200.00);

        memberService.simpleRecharge(testMember.getId(), rechargeAmount);

        Member updatedMember = memberRepository.findById(testMember.getId()).orElseThrow();
        assertEquals(initialBalance.add(rechargeAmount), updatedMember.getBalance());
    }

    @Test
    void testRechargeWithZeroAmount() {
        // Test that recharge with zero amount throws exception
        assertThrows(RuntimeException.class, () -> {
            memberService.simpleRecharge(testMember.getId(), BigDecimal.ZERO);
        });
    }

    @Test
    void testRechargeWithNegativeAmount() {
        // Test that recharge with negative amount throws exception
        assertThrows(RuntimeException.class, () -> {
            memberService.simpleRecharge(testMember.getId(), BigDecimal.valueOf(-50));
        });
    }

    @Test
    void testServiceExecutionDeductsBalance() {
        // Test that creating a service execution deducts balance
        BigDecimal initialBalance = testMember.getBalance();
        BigDecimal serviceCost = testService.getPrice();

        ServiceExecution execution = serviceExecutionService.add(
                testMember.getId(),
                testService.getId(),
                testEmployee.getId(),
                LocalDate.now()
        );

        assertNotNull(execution);
        Member updatedMember = memberRepository.findById(testMember.getId()).orElseThrow();
        assertEquals(initialBalance.subtract(serviceCost), updatedMember.getBalance());
    }

    @Test
    void testBalanceCanBeNegative() {
        // Test that balance can go negative
        testMember.setBalance(BigDecimal.valueOf(10.00));
        memberRepository.save(testMember);

        ServiceExecution execution = serviceExecutionService.add(
                testMember.getId(),
                testService.getId(),
                testEmployee.getId(),
                LocalDate.now()
        );

        assertNotNull(execution);
        Member updatedMember = memberRepository.findById(testMember.getId()).orElseThrow();
        assertTrue(updatedMember.getBalance().compareTo(BigDecimal.ZERO) < 0);
        assertEquals(BigDecimal.valueOf(-40.00), updatedMember.getBalance());
    }

    @Test
    void testCreateFromAppointmentIdempotency() {
        // Test that creating service execution from appointment is idempotent
        Integer appointmentId = 999;
        BigDecimal initialBalance = testMember.getBalance();

        // First call - should create execution and deduct balance
        ServiceExecution execution1 = serviceExecutionService.createFromAppointment(
                appointmentId,
                testMember.getId(),
                testMember.getName(),
                testService.getId(),
                testService.getName(),
                testEmployee.getId(),
                testEmployee.getName(),
                LocalDate.now()
        );

        assertNotNull(execution1);
        Member afterFirst = memberRepository.findById(testMember.getId()).orElseThrow();
        BigDecimal balanceAfterFirst = afterFirst.getBalance();
        assertEquals(initialBalance.subtract(testService.getPrice()), balanceAfterFirst);

        // Second call with same appointmentId - should not create new execution or deduct balance again
        ServiceExecution execution2 = serviceExecutionService.createFromAppointment(
                appointmentId,
                testMember.getId(),
                testMember.getName(),
                testService.getId(),
                testService.getName(),
                testEmployee.getId(),
                testEmployee.getName(),
                LocalDate.now()
        );

        assertNotNull(execution2);
        assertEquals(execution1.getId(), execution2.getId());
        
        Member afterSecond = memberRepository.findById(testMember.getId()).orElseThrow();
        assertEquals(balanceAfterFirst, afterSecond.getBalance());
    }

    @Test
    void testDeductBalance() {
        // Test direct balance deduction
        BigDecimal initialBalance = testMember.getBalance();
        BigDecimal deductAmount = BigDecimal.valueOf(30.00);

        memberService.deductBalance(testMember.getId(), deductAmount);

        Member updatedMember = memberRepository.findById(testMember.getId()).orElseThrow();
        assertEquals(initialBalance.subtract(deductAmount), updatedMember.getBalance());
    }
}
