package com.barbershop.system.controller;

import com.barbershop.system.entity.Appointment;
import com. barbershop.system.service.AppointmentService;
import org.springframework.beans.factory. annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @GetMapping
    public List<Appointment> getAll() {
        return appointmentService.findAll();
    }

    @GetMapping("/search")
    public List<Appointment> search(@RequestParam String keyword) {
        return appointmentService.search(keyword);
    }

    @PostMapping
    public ResponseEntity<?> addAppointment(@RequestBody Map<String, Object> params) {
        try {
            Integer memberId = Integer. parseInt(params.get("memberId").toString());
            Integer serviceId = Integer.parseInt(params.get("serviceId").toString());
            Integer employeeId = Integer.parseInt(params.get("employeeId").toString());
            LocalDate date = LocalDate.parse(params. get("date").toString());
            Integer startHour = Integer.parseInt(params.get("startHour").toString());

            Appointment appointment = appointmentService.add(memberId, serviceId, employeeId, date, startHour);
            return ResponseEntity.ok(appointment);
        } catch (RuntimeException e) {
            // 返回标准的JSON错误格式
            Map<String, String> error = new HashMap<>();
            error. put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "预约失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<?> complete(@PathVariable Integer id) {
        try {
            appointmentService.complete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "完成预约失败：" + e. getMessage());
            return ResponseEntity. badRequest().body(error);
        }
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Integer id) {
        try {
            appointmentService.cancel(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "取消预约失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            appointmentService.delete(id);
            return ResponseEntity. ok().build();
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "删除预约失败：" + e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}