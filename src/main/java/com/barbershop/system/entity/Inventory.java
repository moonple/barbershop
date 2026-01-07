package com.barbershop.system.entity; // 必须是这个包名

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;           // 物品名称
    private Integer quantity;      // 当前库存
    private Integer threshold;     // 警戒线
    private String remark;         // 备注

    public void updateRemark() {
        if (quantity == null) quantity = 0;
        if (threshold == null) threshold = 0;
        if (quantity < 0) quantity = 0;

        if (quantity < threshold) {
            this.remark = "⚠️ 库存不足，请补货！";
        } else {
            this.remark = "库存充足";
        }
    }
}