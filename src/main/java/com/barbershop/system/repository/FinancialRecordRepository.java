package com.barbershop.system.repository;

import com.barbershop.system.entity.FinancialRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Integer> {

    List<FinancialRecord> findByMemberIdOrderByCreateTimeDesc(Integer memberId);
}