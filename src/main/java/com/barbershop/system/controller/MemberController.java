package com.barbershop.system.controller;

import com.barbershop.system.entity.FinancialRecord;
import com.barbershop.system.entity.Member;
import com.barbershop.system.service.MemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @GetMapping
    public List<Member> getAll() {
        return memberService.findAllMembers();
    }

    // 添加会员
    @PostMapping
    public Member add(@RequestBody Member member) {
        if (member.getName() == null || member.getName().trim().isEmpty()) {
            throw new RuntimeException("会员姓名不能为空");
        }
        if (member.getPhone() == null || member.getPhone().trim().isEmpty()) {
            throw new RuntimeException("会员手机号不能为空");
        }
        if (!member.getPhone().matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号格式错误");
        }

        return memberService.addMember(member);
    }

    //修改信息
    @PutMapping("/{id}")
    public Member update(@PathVariable Integer id, @RequestBody Member member) {
        return memberService.updateMember(id, member);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        memberService.deleteMember(id);
    }

    @PostMapping("/{id}/recharge")
    public void recharge(@PathVariable Integer id, @RequestBody Map<String, BigDecimal> body) {
        memberService.recharge(id, body.get("amount"));
    }

    // 简单充值接口
    @PostMapping("/{id}/simple-recharge")
    public void simpleRecharge(@PathVariable Integer id, @RequestBody Map<String, BigDecimal> body) {
        memberService.simpleRecharge(id, body.get("amount"));
    }

    @GetMapping("/{id}/records")
    public List<FinancialRecord> getRecords(@PathVariable Integer id) {
        return memberService.getMemberRecords(id);
    }

    // 会员搜索接口
    @GetMapping("/search")
    public List<Member> search(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return memberService.findAllMembers();
        }
        return memberService.searchMembers(keyword);
    }


}