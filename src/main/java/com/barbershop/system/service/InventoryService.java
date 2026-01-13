package com.barbershop.system.service;

import com.barbershop.system.entity.Inventory;
import com.barbershop.system.repository.InventoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    @Autowired
    private InventoryRepository inventoryRepository;

    public List<Inventory> findAll() {
        return inventoryRepository.findAll();
    }

    // 添加
    public Inventory add(Inventory item) {
        item.updateRemark(); // 计算备注
        return inventoryRepository.save(item);
    }

    // 修改 (名称、库存、警戒线)
    public Inventory update(Inventory item) {
        Inventory old = inventoryRepository.findById(item.getId()).orElseThrow();

        if (item.getName() != null) old.setName(item.getName());
        if (item.getQuantity() != null) old.setQuantity(item.getQuantity());
        if (item.getThreshold() != null) old.setThreshold(item.getThreshold());

        old.updateRemark();
        return inventoryRepository.save(old);
    }

    public void delete(Integer id) {
        inventoryRepository.deleteById(id);
    }

    // 搜索 (ID 或 名称)
    public List<Inventory> search(String keyword) {
        if (keyword.matches("\\d+")) {
            Optional<Inventory> byId = inventoryRepository.findById(Integer.parseInt(keyword));
            if (byId.isPresent()) return List.of(byId.get());
        }
        return inventoryRepository.findByNameContaining(keyword);
    }
}