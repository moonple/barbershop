package com.barbershop.system.repository;

import com.barbershop.system.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Integer> {


    @Query("SELECT a FROM Appointment a WHERE CAST(a.id AS string) LIKE %?1% OR CAST(a.memberId AS string) LIKE %?1% OR CAST(a.employeeId AS string) LIKE %?1%")
    List<Appointment> searchByKeyword(String keyword);


    List<Appointment> findByAppointmentDate(LocalDate date);


    @Query("SELECT a FROM Appointment a WHERE a.employeeId = ?1 AND a.status <> '已取消' AND a.startTime < ?3 AND a.endTime > ?2")
    List<Appointment> findConflicts(Integer empId, LocalDateTime newStart, LocalDateTime newEnd);
}