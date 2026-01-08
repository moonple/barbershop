package com.barbershop.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购记录实体
 * 用于记录物品的采购历史
 */
@Entity
@Data
public class PurchaseRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // 物品信息
    private Integer inventoryId;
    private String inventoryName;

    // 采购数量
    private Integer quantity;

    // 单价
    private BigDecimal unitPrice;

    // 总价 (quantity * unitPrice)
    private BigDecimal totalPrice;

    // 采购日期
    private LocalDate purchaseDate;

    @PrePersist
    public void prePersist() {
        if (purchaseDate == null) {
            purchaseDate = LocalDate.now();
        }
        // 计算总价
        if (quantity != null && unitPrice != null) {
            totalPrice = unitPrice.multiply(new BigDecimal(quantity));
        }
    }

    @PreUpdate
    public void preUpdate() {
        // 更新时重新计算总价
        if (quantity != null && unitPrice != null) {
            totalPrice = unitPrice.multiply(new BigDecimal(quantity));
        }
    }
}
