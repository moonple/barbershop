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

    // 修改会员
    public Member updateMember(Integer id, Member newInfo) {

        Member oldMember = memberRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("会员不存在"));

        if (newInfo.getName() != null && !newInfo.getName().trim().isEmpty()) {
            oldMember.setName(newInfo.getName());
        }
        if (newInfo.getPhone() != null && !newInfo.getPhone().trim().isEmpty()) {
            oldMember.setPhone(newInfo.getPhone());
        }
        if (newInfo.getGender() != null && !newInfo.getGender().trim().isEmpty()) {
            oldMember.setGender(newInfo.getGender());
        }

        return memberRepository.save(oldMember);
    }

    public void deleteMember(Integer id) {
        memberRepository.deleteById(id);
    }

    // 充值逻
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

    // 简单充值逻辑
    @Transactional
    public void simpleRecharge(Integer memberId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("会员不存在"));
        BigDecimal currentBalance = member.getBalance();
        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }
        member.setBalance(currentBalance.add(amount));
        memberRepository.save(member);
    }

    // 扣除余额
    @Transactional
    public void deductBalance(Integer memberId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("扣费金额必须大于0");
        }
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new RuntimeException("会员不存在"));
        BigDecimal currentBalance = member.getBalance();
        if (currentBalance == null) {
            currentBalance = BigDecimal.ZERO;
        }

        member.setBalance(currentBalance.subtract(amount));
        memberRepository.save(member);
    }

    public List<FinancialRecord> getMemberRecords(Integer memberId) {
        return financialRecordRepository.findByMemberIdOrderByCreateTimeDesc(memberId);
    }

    public List<Member> searchMembers(String keyword) {

        return memberRepository.findByPhoneContaining(keyword);
    }
}