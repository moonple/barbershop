package com.barbershop.system.repository;

import com.barbershop.system.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Integer> {
    // 模糊查询名称
    List<Inventory> findByNameContaining(String name);
}