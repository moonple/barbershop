package com.barbershop.system.service;

import com.barbershop. system.entity.Appointment;
import com.barbershop.system.entity.Employee;
import com.barbershop.system.entity.Inventory;
import com. barbershop.system.entity. Member;
import com.barbershop.system.entity.ServiceItem;

import com.barbershop.system.repository.*;
import org.springframework.beans. factory.annotation.Autowired;
import org.springframework.stereotype. Service;
import org.springframework. transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time. LocalDateTime;
import java.time.LocalTime;
import java. util.List;

@Service
public class AppointmentService {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private MemberRepository memberRepository;
    @Autowired private EmployeeRepository employeeRepository;
    @Autowired private ServiceItemRepository serviceItemRepository;
    @Autowired private InventoryRepository inventoryRepository;

    public List<Appointment> findAll() { return appointmentRepository. findAll(); }

    public List<Appointment> search(String keyword) {
        if (keyword. matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return appointmentRepository.findByAppointmentDate(LocalDate.parse(keyword));
        }
        return appointmentRepository.searchByKeyword(keyword);
    }

    @Transactional
    public Appointment add(Integer mId, Integer sId, Integer eId, LocalDate date, Integer startHour) {
        Member m = memberRepository.findById(mId).orElseThrow(() -> new RuntimeException("会员不存在"));
        Employee e = employeeRepository.findById(eId).orElseThrow(() -> new RuntimeException("员工不存在"));

        // ✅ 修复：添加变量名 s
        ServiceItem s = serviceItemRepository.findById(sId).orElseThrow(() -> new RuntimeException("服务不存在"));

        LocalDateTime start = LocalDateTime.of(date, LocalTime. of(startHour, 0));
        LocalDateTime end = start.plusMinutes(s.getDuration());

        // 营业时间校验 (9-12, 14-17)
        checkBusinessHours(start, end);

        // 冲突检测
        List<Appointment> conflicts = appointmentRepository. findConflicts(eId, start, end);
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("冲突：该员工在 " + startHour + "点 时段已有预约！");
        }

        Appointment app = new Appointment();
        app.setMemberId(mId); app.setMemberName(m. getName());
        app.setServiceId(sId); app.setServiceName(s.getName());
        app.setEmployeeId(eId); app.setEmployeeName(e.getName());
        app.setAppointmentDate(date);
        app.setStartTime(start);
        app.setEndTime(end);
        app.setTimeRange(String. format("%02d:%02d - %02d:%02d", start.getHour(), start.getMinute(), end.getHour(), end.getMinute()));
        app.setStatus("待服务");
        app.setRemark(LocalDate.now().toString());

        return appointmentRepository. save(app);
    }

    @Transactional
    public void complete(Integer id) {
        Appointment app = appointmentRepository.findById(id).orElseThrow();
        if (!"待服务".equals(app.getStatus())) return;

        app.setStatus("已服务");
        appointmentRepository.save(app);

        // 扣库存
        deductInventory(app.getServiceId());
    }

    public void cancel(Integer id) {
        Appointment app = appointmentRepository.findById(id).orElseThrow();
        app.setStatus("已取消");
        appointmentRepository.save(app);
    }

    public void delete(Integer id) {
        appointmentRepository.deleteById(id);
    }

    private void checkBusinessHours(LocalDateTime start, LocalDateTime end) {
        int sH = start.getHour();
        int eH = end.getHour();
        if (end.getMinute() > 0) eH++;

        if (sH < 9 || eH > 17) throw new RuntimeException("不在营业时间(09:00-17:00)");
        if ((sH >= 12 && sH < 14) || (sH < 12 && eH > 12)) throw new RuntimeException("12:00-14:00 为午休时间");
    }

    private void deductInventory(Integer sId) {
        // ✅ 修复：添加变量名 s
        ServiceItem s = serviceItemRepository.findById(sId).orElse(null);
        if (s != null && s.getConsumeItemIds() != null && !s.getConsumeItemIds().isEmpty()) {
            for (String idStr : s. getConsumeItemIds().split(",")) {
                if (idStr.isEmpty()) continue;
                try {
                    Integer itemId = Integer.parseInt(idStr);
                    inventoryRepository.findById(itemId).ifPresent(inv -> {
                        if (inv.getQuantity() > 0) {
                            inv.setQuantity(inv.getQuantity() - 1);
                            inv.updateRemark();
                            inventoryRepository.save(inv);
                        }
                    });
                } catch (Exception ignored) {}
            }
        }
    }
}