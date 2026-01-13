package com.barbershop.system.controller;

import com.barbershop.system.entity.ServiceExecution;
import com.barbershop.system.service.ServiceExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/service-executions")
public class ServiceExecutionController {

    @Autowired
    private ServiceExecutionService serviceExecutionService;


    @GetMapping
    public List<ServiceExecution> getAll() {
        return serviceExecutionService.findAll();
    }

    @GetMapping("/search")
    public List<ServiceExecution> search(
            @RequestParam(required = false) Integer memberId,
            @RequestParam(required = false) Integer serviceId,
            @RequestParam(required = false) Integer employeeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate serviceDate) {
        return serviceExecutionService.findByFilters(memberId, serviceId, employeeId, serviceDate);
    }


    @PostMapping
    public ResponseEntity<?> add(@RequestBody Map<String, Object> params) {
        try {
            Integer memberId = Integer.parseInt(params.get("memberId").toString());
            Integer serviceId = Integer.parseInt(params.get("serviceId").toString());
            Integer employeeId = Integer.parseInt(params.get("employeeId").toString());
            
            LocalDate serviceDate = null;
            if (params.containsKey("serviceDate") && params.get("serviceDate") != null 
                && !params.get("serviceDate").toString().isEmpty()) {
                serviceDate = LocalDate.parse(params.get("serviceDate").toString());
            }

            ServiceExecution execution = serviceExecutionService.add(memberId, serviceId, employeeId, serviceDate);
            return ResponseEntity.ok(execution);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "新增失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> params) {
        try {
            Integer memberId = params.containsKey("memberId") && params.get("memberId") != null 
                ? Integer.parseInt(params.get("memberId").toString()) : null;
            Integer serviceId = params.containsKey("serviceId") && params.get("serviceId") != null 
                ? Integer.parseInt(params.get("serviceId").toString()) : null;
            Integer employeeId = params.containsKey("employeeId") && params.get("employeeId") != null 
                ? Integer.parseInt(params.get("employeeId").toString()) : null;
            
            LocalDate serviceDate = null;
            if (params.containsKey("serviceDate") && params.get("serviceDate") != null 
                && !params.get("serviceDate").toString().isEmpty()) {
                serviceDate = LocalDate.parse(params.get("serviceDate").toString());
            }

            ServiceExecution execution = serviceExecutionService.update(id, memberId, serviceId, employeeId, serviceDate);
            return ResponseEntity.ok(execution);
        } catch (RuntimeException e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "修改失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            serviceExecutionService.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "删除失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
