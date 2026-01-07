package com.barbershop.system.controller;

import com.barbershop.system.entity.ServiceItem;
import com.barbershop.system.repository.ServiceItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceItemController {

    @Autowired
    private ServiceItemRepository serviceItemRepository;

    @GetMapping
    public List<ServiceItem> getAll() {
        return serviceItemRepository.findAll();
    }

    @GetMapping("/search")
    public List<ServiceItem> search(@RequestParam String keyword) {
        if (keyword.matches("\\d+")) {
            return serviceItemRepository.findById(Integer.parseInt(keyword))
                    .map(List::of).orElse(List.of());
        }
        return serviceItemRepository.findByNameContaining(keyword);
    }

    @PostMapping
    public ServiceItem add(@RequestBody ServiceItem item) {
        validate(item);
        return serviceItemRepository.save(item);
    }

    @PutMapping("/{id}")
    public ServiceItem update(@PathVariable Integer id, @RequestBody ServiceItem newItem) {
        ServiceItem old = serviceItemRepository.findById(id).orElseThrow();

        if(newItem.getName() != null) old.setName(newItem.getName());
        if(newItem.getPrice() != null) old.setPrice(newItem.getPrice());
        if(newItem.getDuration() != null) {
            if(newItem.getDuration() > 180) throw new RuntimeException("服务用时不能超过3小时(180分钟)");
            old.setDuration(newItem.getDuration());
        }
        // 更新消耗品
        old.setConsumeItemIds(newItem.getConsumeItemIds());

        return serviceItemRepository.save(old);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        serviceItemRepository.deleteById(id);
    }

    private void validate(ServiceItem item) {
        if (item.getName() == null || item.getName().isEmpty()) throw new RuntimeException("名称不能为空");
        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) < 0) throw new RuntimeException("价格不能小于0");
        if (item.getDuration() == null || item.getDuration() <= 0) throw new RuntimeException("用时必须大于0");
        if (item.getDuration() > 180) throw new RuntimeException("服务用时不能超过3小时(180分钟)");
    }
}