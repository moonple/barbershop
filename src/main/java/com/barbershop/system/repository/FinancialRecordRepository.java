package com.barbershop.system.repository;

import com.barbershop.system.entity.FinancialRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Integer> {
    // 自动生成 SQL：根据会员ID查询记录，按时间倒序
    List<FinancialRecord> findByMemberIdOrderByCreateTimeDesc(Integer memberId);
}