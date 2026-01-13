# 会员余额充值与服务扣费功能文档

## 功能概述

本次更新实现了会员余额充值功能和自动服务扣费功能，并确保通过预约创建的服务执行记录不会重复扣费。

## 核心功能

### 1. 会员余额充值

#### 后端接口
- **URL**: `POST /api/members/{id}/simple-recharge`
- **参数**: 
  ```json
  {
    "amount": 100.00
  }
  ```
- **验证**: 
  - 充值金额必须大于 0
  - 会员必须存在
- **特点**: 
  - 不创建财务流水记录（FinancialRecord）
  - 直接增加会员余额

#### 前端使用
1. 在会员管理页面，每个会员行有一个"💰充值"按钮
2. 点击按钮打开充值弹窗
3. 输入充值金额（必须大于0）
4. 点击"确认充值"完成充值
5. 充值后余额立即更新显示

### 2. 服务执行自动扣费

#### 手动创建服务执行记录
- 在"服务执行管理"页面点击"+ 新增服务执行记录"
- 选择会员、服务项目、员工和日期
- 保存后自动扣除会员余额（扣除金额 = 服务价格）

#### 预约完成自动扣费
- 在"预约管理"页面，将预约状态从"待服务"改为"已服务"
- 系统自动创建服务执行记录
- 自动扣除会员余额（仅扣费一次）
- 通过 `appointmentId` 的唯一约束确保不重复扣费

### 3. 幂等性保证

系统通过以下机制确保不重复扣费：

1. **ServiceExecution 表的 appointmentId 字段设置为唯一**
2. **创建服务执行记录前检查**：
   - 检查是否已存在相同 appointmentId 的记录
   - 如果存在，直接返回已有记录，不重复创建和扣费
   - 如果不存在，创建新记录并扣费

## 技术实现

### 后端代码修改

#### MemberService.java
```java
// 简单充值（不记录财务流水）
@Transactional
public void simpleRecharge(Integer memberId, BigDecimal amount) {
    if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new RuntimeException("充值金额必须大于0");
    }
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new RuntimeException("会员不存在"));
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
    if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
        throw new RuntimeException("扣费金额不能为负数");
    }
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new RuntimeException("会员不存在"));
    BigDecimal currentBalance = member.getBalance();
    if (currentBalance == null) {
        currentBalance = BigDecimal.ZERO;
    }
    // 允许余额为负数
    member.setBalance(currentBalance.subtract(amount));
    memberRepository.save(member);
}
```

#### ServiceExecutionService.java
```java
@Transactional
public ServiceExecution createFromAppointment(...) {
    // 检查是否已存在（幂等性控制）
    Optional<ServiceExecution> existing = 
        serviceExecutionRepository.findByAppointmentId(appointmentId);
    if (existing.isPresent()) {
        return existing.get(); // 已存在，直接返回，不重复扣费
    }
    
    // 创建新记录
    ServiceExecution execution = new ServiceExecution();
    // ... 设置字段
    ServiceExecution savedExecution = serviceExecutionRepository.save(execution);
    
    // 扣除会员余额（仅在创建新记录时）
    memberService.deductBalance(memberId, serviceItem.getPrice());
    
    return savedExecution;
}
```

### 前端代码修改

#### index.html
1. 添加充值按钮到会员表格
2. 添加充值弹窗 HTML
3. 实现充值相关 JavaScript 函数

## 测试

### 自动化测试
运行命令：
```bash
./mvnw test -Dtest=MemberBalanceServiceTest
```

测试覆盖：
- ✅ 简单充值功能
- ✅ 充值金额验证（零和负数）
- ✅ 服务执行自动扣费
- ✅ 余额可以为负数
- ✅ 预约创建服务执行的幂等性
- ✅ 重复调用不重复扣费
- ✅ 直接余额扣除

所有测试通过：**7/7**

### 手动测试步骤

#### 测试充值功能
1. 启动应用
2. 登录系统
3. 进入"会员管理"
4. 点击某个会员的"💰充值"按钮
5. 输入充值金额（如 100）
6. 确认充值
7. 验证余额正确增加

#### 测试服务扣费
1. 进入"服务执行管理"
2. 点击"+ 新增服务执行记录"
3. 选择会员、服务、员工
4. 保存
5. 返回"会员管理"查看该会员余额是否减少

#### 测试预约扣费不重复
1. 进入"预约管理"
2. 创建一个新预约（选择会员、服务、员工）
3. 记录该会员的当前余额
4. 将预约状态改为"已服务"
5. 验证会员余额减少了服务价格
6. 再次点击"已服务"按钮
7. 验证余额没有再次减少

## 注意事项

1. **余额允许为负数**：系统不会阻止服务执行即使余额不足
2. **不创建财务记录**：简单充值不会在 FinancialRecord 表中创建记录
3. **幂等性**：同一预约多次完成只会扣费一次
4. **事务性**：所有余额操作都在事务中进行，保证数据一致性

## API 接口

### 会员充值
```
POST /api/members/{id}/simple-recharge
Content-Type: application/json

{
  "amount": 100.00
}

Response: 200 OK (成功)
Response: 4xx (失败，返回错误信息)
```

### 余额扣除（内部使用）
```java
memberService.deductBalance(memberId, amount);
```

### 创建服务执行（带扣费）
```
POST /api/service-executions
Content-Type: application/json

{
  "memberId": 1,
  "serviceId": 2,
  "employeeId": 3,
  "serviceDate": "2026-01-13"
}

Response: 200 OK (成功，自动扣费)
```

## 数据库变更

无需额外的数据库变更。现有的 `ServiceExecution` 表已经有 `appointmentId` 字段的唯一约束。

## 未来增强

1. 可以考虑添加充值历史记录（不同于财务流水）
2. 可以添加余额变动通知功能
3. 可以添加余额不足警告（虽然不阻止服务）
