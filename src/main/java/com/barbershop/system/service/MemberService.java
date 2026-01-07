package com.barbershop.system.service;

import com.barbershop.system.entity.FinancialRecord;
import com.barbershop.system.entity.Member;
import com.barbershop.system.repository.FinancialRecordRepository;
import com.barbershop.system.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private FinancialRecordRepository financialRecordRepository;

    public List<Member> findAllMembers() {
        return memberRepository.findAll();
    }

    public Member addMember(Member member) {
        if (memberRepository.findByPhone(member.getPhone()) != null) {
            throw new RuntimeException("手机号已存在！");
        }
        return memberRepository.save(member);
    }

    // --- 修复重点：修改会员逻辑 ---
    public Member updateMember(Integer id, Member newInfo) {
        // 1. 先查出数据库里的老数据
        Member oldMember = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("会员不存在"));

        // 2. 如果前端传了新值，就更新；没传（或为空），就保持原样
        if (newInfo.getName() != null && !newInfo.getName().trim().isEmpty()) {
            oldMember.setName(newInfo.getName());
        }
        if (newInfo.getPhone() != null && !newInfo.getPhone().trim().isEmpty()) {
            oldMember.setPhone(newInfo.getPhone());
        }
        if (newInfo.getGender() != null && !newInfo.getGender().trim().isEmpty()) {
            oldMember.setGender(newInfo.getGender());
        }

        // 3. 保存更新后的对象
        return memberRepository.save(oldMember);
    }

    public void deleteMember(Integer id) {
        memberRepository.deleteById(id);
    }

    // 充值逻辑 (保留)
    @Transactional
    public void recharge(Integer memberId, BigDecimal amount) {
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("会员不存在"));
        member.setBalance(member.getBalance().add(amount));
        memberRepository.save(member);

        FinancialRecord record = new FinancialRecord();
        record.setMemberId(memberId);
        record.setType("充值");
        record.setAmount(amount);
        financialRecordRepository.save(record);
    }

    public List<FinancialRecord> getMemberRecords(Integer memberId) {
        return financialRecordRepository.findByMemberIdOrderByCreateTimeDesc(memberId);
    }

    public List<Member> searchMembers(String keyword) {
        // 调用刚才在 Repository 里写的方法
        return memberRepository.findByPhoneContaining(keyword);
    }
}