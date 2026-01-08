package com.barbershop.system.controller;

import com.barbershop.system.entity.Inventory;
import com.barbershop.system.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryService inventoryService;

    @GetMapping
    public List<Inventory> getAll() {
        return inventoryService.findAll();
    }

    @PostMapping
    public Inventory add(@RequestBody Inventory item) {
        if(item.getName() == null || item.getName().isEmpty()) throw new RuntimeException("名称不能为空");
        if(item.getQuantity() < 0) throw new RuntimeException("库存不能小于0");
        if(item.getThreshold() < 0) throw new RuntimeException("警戒线不能小于0");
        return inventoryService.add(item);
    }

    @PutMapping
    public Inventory update(@RequestBody Inventory item) {
        if(item.getQuantity() != null && item.getQuantity() < 0) throw new RuntimeException("库存不能小于0");
        return inventoryService.update(item);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        inventoryService.delete(id);
    }

    @GetMapping("/search")
    public List<Inventory> search(@RequestParam String keyword) {
        if(keyword == null || keyword.isEmpty()) return inventoryService.findAll();
        return inventoryService.search(keyword);
    }
}