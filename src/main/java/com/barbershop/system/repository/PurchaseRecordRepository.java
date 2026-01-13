package com.barbershop.system.repository;

import com.barbershop.system.entity.PurchaseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


public interface PurchaseRecordRepository extends JpaRepository<PurchaseRecord, Integer> {
    
    // 根据物品ID查询
    List<PurchaseRecord> findByInventoryId(Integer inventoryId);
    
    // 根据日期查询
    List<PurchaseRecord> findByPurchaseDate(LocalDate date);
    
    // 组合查询
    @Query("SELECT pr FROM PurchaseRecord pr WHERE " +
           "(:inventoryId IS NULL OR pr.inventoryId = :inventoryId) AND " +
           "(:purchaseDate IS NULL OR pr.purchaseDate = :purchaseDate)")
    List<PurchaseRecord> findByFilters(@Param("inventoryId") Integer inventoryId, 
                                       @Param("purchaseDate") LocalDate purchaseDate);
    
    // 计算总支出（根据筛选条件）
    @Query("SELECT COALESCE(SUM(pr.totalPrice), 0) FROM PurchaseRecord pr WHERE " +
           "(:inventoryId IS NULL OR pr.inventoryId = :inventoryId) AND " +
           "(:purchaseDate IS NULL OR pr.purchaseDate = :purchaseDate)")
    BigDecimal sumTotalByFilters(@Param("inventoryId") Integer inventoryId, 
                                  @Param("purchaseDate") LocalDate purchaseDate);
}
