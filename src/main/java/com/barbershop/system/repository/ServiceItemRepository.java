package com.barbershop.system.repository;

import com.barbershop.system.entity.ServiceItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Integer> {
    List<ServiceItem> findByNameContaining(String name);
}