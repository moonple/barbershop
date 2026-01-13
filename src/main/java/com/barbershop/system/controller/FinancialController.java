package com.barbershop.system.controller;

import com.barbershop.system.entity.Inventory;
import com.barbershop.system.entity.PurchaseRecord;
import com.barbershop.system.entity.ServiceExecution;
import com.barbershop.system.repository.InventoryRepository;
import com.barbershop.system.repository.PurchaseRecordRepository;
import com.barbershop.system.repository.ServiceExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/financial")
public class FinancialController {

    @Autowired
    private ServiceExecutionRepository serviceExecutionRepository;

    @Autowired
    private PurchaseRecordRepository purchaseRecordRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    // ==================== 服务收入模块 ====================

    @GetMapping("/income")
    public ResponseEntity<Map<String, Object>> getIncomeRecords(
            @RequestParam(required = false) Integer memberId,
            @RequestParam(required = false) Integer serviceId,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate serviceDate) {
        
        List<ServiceExecution> records = serviceExecutionRepository.findByFilters(memberId, serviceId, employeeId, serviceDate);
        BigDecimal totalIncome = serviceExecutionRepository.sumCostByFilters(memberId, serviceId, employeeId, serviceDate);
        
        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("totalIncome", totalIncome);
        
        return ResponseEntity.ok(response);
    }

    // ==================== 采购支出模块 ====================


    @GetMapping("/purchase")
    public ResponseEntity<Map<String, Object>> getPurchaseRecords(
            @RequestParam(required = false) Integer inventoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate purchaseDate) {
        
        List<PurchaseRecord> records = purchaseRecordRepository.findByFilters(inventoryId, purchaseDate);
        BigDecimal totalExpenditure = purchaseRecordRepository.sumTotalByFilters(inventoryId, purchaseDate);
        
        Map<String, Object> response = new HashMap<>();
        response.put("records", records);
        response.put("totalExpenditure", totalExpenditure);
        
        return ResponseEntity.ok(response);
    }


    @PostMapping("/purchase")
    @Transactional
    public ResponseEntity<?> addPurchase(@RequestBody Map<String, Object> params) {
        try {
            Integer inventoryId = Integer.parseInt(params.get("inventoryId").toString());
            Integer quantity = Integer.parseInt(params.get("quantity").toString());
            BigDecimal unitPrice = new BigDecimal(params.get("unitPrice").toString());

            if (quantity <= 0) {
                throw new RuntimeException("采购数量必须大于0");
            }
            if (unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("单价必须大于0");
            }

            // 查找库存物品
            Inventory inventory = inventoryRepository.findById(inventoryId)
                    .orElseThrow(() -> new RuntimeException("库存物品不存在"));

            // 创建采购记录
            PurchaseRecord record = new PurchaseRecord();
            record.setInventoryId(inventoryId);
            record.setInventoryName(inventory.getName());
            record.setQuantity(quantity);
            record.setUnitPrice(unitPrice);
            
            // 保存采购记录
            PurchaseRecord savedRecord = purchaseRecordRepository.save(record);

            // 更新库存数量
            inventory.setQuantity(inventory.getQuantity() + quantity);
            inventory.updateRemark();
            inventoryRepository.save(inventory);

            return ResponseEntity.ok(savedRecord);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "新增采购记录失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }


    @DeleteMapping("/purchase/{id}")
    @Transactional
    public ResponseEntity<?> deletePurchase(@PathVariable Integer id) {
        try {
            purchaseRecordRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "删除失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
