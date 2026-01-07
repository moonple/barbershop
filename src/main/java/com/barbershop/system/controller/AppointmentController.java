package com.barbershop.system.controller;

import com.barbershop.system.entity.Appointment;
import com.barbershop.system.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired private AppointmentService appointmentService;

    @GetMapping
    public List<Appointment> getAll() { return appointmentService.findAll(); }

    @GetMapping("/search")
    public List<Appointment> search(@RequestParam String keyword) { return appointmentService.search(keyword); }

    @PostMapping
    public Appointment add(@RequestBody Map<String, String> body) {
        Integer mId = Integer.parseInt(body.get("memberId"));
        Integer sId = Integer.parseInt(body.get("serviceId"));
        Integer eId = Integer.parseInt(body.get("employeeId"));
        LocalDate date = LocalDate.parse(body.get("date"));
        Integer startHour = Integer.parseInt(body.get("startHour"));

        return appointmentService.add(mId, sId, eId, date, startHour);
    }

    @PostMapping("/{id}/complete")
    public void complete(@PathVariable Integer id) { appointmentService.complete(id); }

    @PostMapping("/{id}/cancel")
    public void cancel(@PathVariable Integer id) { appointmentService.cancel(id); }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) { appointmentService.delete(id); }
}