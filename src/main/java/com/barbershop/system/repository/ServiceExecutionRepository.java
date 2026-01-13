package com.barbershop.system.repository;

import com.barbershop.system.entity.ServiceExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ServiceExecutionRepository extends JpaRepository<ServiceExecution, Integer> {
    
    // 根据预约ID查找（用于幂等性检查）
    Optional<ServiceExecution> findByAppointmentId(Integer appointmentId);
    
    // 根据会员ID查询
    List<ServiceExecution> findByMemberId(Integer memberId);
    
    // 根据服务ID查询
    List<ServiceExecution> findByServiceId(Integer serviceId);
    
    // 根据员工ID查询
    List<ServiceExecution> findByEmployeeId(Integer employeeId);
    
    // 根据日期查询
    List<ServiceExecution> findByServiceDate(LocalDate date);
    
    // 组合查询（支持null值）
    @Query("SELECT se FROM ServiceExecution se WHERE " +
           "(:memberId IS NULL OR se.memberId = :memberId) AND " +
           "(:serviceId IS NULL OR se.serviceId = :serviceId) AND " +
           "(:employeeId IS NULL OR se.employeeId = :employeeId) AND " +
           "(:serviceDate IS NULL OR se.serviceDate = :serviceDate)")
    List<ServiceExecution> findByFilters(@Param("memberId") Integer memberId, 
                                        @Param("serviceId") Integer serviceId, 
                                        @Param("employeeId") Integer employeeId, 
                                        @Param("serviceDate") LocalDate serviceDate);
    
    // 计算总收入（根据筛选条件）
    @Query("SELECT COALESCE(SUM(se.cost), 0) FROM ServiceExecution se WHERE " +
           "(:memberId IS NULL OR se.memberId = :memberId) AND " +
           "(:serviceId IS NULL OR se.serviceId = :serviceId) AND " +
           "(:employeeId IS NULL OR se.employeeId = :employeeId) AND " +
           "(:serviceDate IS NULL OR se.serviceDate = :serviceDate)")
    BigDecimal sumCostByFilters(@Param("memberId") Integer memberId, 
                                @Param("serviceId") Integer serviceId, 
                                @Param("employeeId") Integer employeeId, 
                                @Param("serviceDate") LocalDate serviceDate);
}
