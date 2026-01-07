package com.barbershop.system.repository;

import com.barbershop.system.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // 记得导入List

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
    // 查重用
    Member findByPhone(String phone);

    // 搜索用 (模糊匹配)
    List<Member> findByPhoneContaining(String phone);
}