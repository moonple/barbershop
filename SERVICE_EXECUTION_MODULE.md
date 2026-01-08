# 服务执行管理模块文档

## 模块概述

服务执行管理模块用于记录每次服务的实际执行情况，与预约管理模块协同工作，提供完整的服务流程管理。

## 功能特性

### 1. 数据字段

每条服务执行记录包含以下字段：

- **id**: 自增主键，按顺序自动分配
- **会员ID (memberId)**: 关联的会员ID
- **会员姓名 (memberName)**: 会员姓名
- **服务项目ID (serviceId)**: 关联的服务项目ID
- **服务项目名称 (serviceName)**: 服务项目名称
- **员工ID (employeeId)**: 执行服务的员工ID
- **员工姓名 (employeeName)**: 员工姓名
- **费用 (cost)**: 服务费用，自动从服务项目中读取
- **服务日期 (serviceDate)**: 服务执行日期
- **预约ID (appointmentId)**: 关联的预约记录ID（用于幂等性控制）

### 2. 列表功能

在服务执行管理页面可以：

- 查看所有服务执行记录
- 查看详细的执行信息，包括会员、服务项目、员工、费用和日期
- 点击"修改"按钮编辑记录
- 点击"删除"按钮删除记录

### 3. 新增功能

通过弹窗新增服务执行记录：

1. 点击"+ 新增服务执行记录"按钮
2. 在弹窗中选择：
   - 会员（下拉列表）
   - 服务项目（下拉列表，显示价格）
   - 员工（下拉列表）
   - 服务日期（日期选择器，默认为当天）
3. 点击"保存"提交

### 4. 修改功能

修改现有服务执行记录：

1. 点击记录行的"修改"按钮
2. 在弹窗中可修改：
   - 会员
   - 服务项目
   - 员工
   - 服务日期
3. 点击"保存"更新记录

**注意**: 修改服务项目时，费用会自动更新为新服务项目的价格。

### 5. 删除功能

- 点击"删除"按钮可删除服务执行记录
- 删除操作会要求确认，删除后不可恢复

### 6. 查询功能

支持多条件筛选查询：

- **会员筛选**: 从下拉列表选择特定会员
- **服务项目筛选**: 从下拉列表选择特定服务项目
- **员工筛选**: 从下拉列表选择特定员工
- **日期筛选**: 选择特定日期

可单独使用任一条件，也可组合多个条件进行精确查询。点击"查询"按钮执行筛选，点击"重置"按钮清空条件并显示所有记录。

### 7. 自动创建功能（与预约模块联动）

**核心特性**: 当预约管理模块中某条预约记录的状态从"待服务"变为"已服务"时，系统会自动创建对应的服务执行记录。

#### 自动创建流程：

1. 在预约管理页面，点击预约记录的"✅ 已服务"按钮
2. 系统自动执行以下操作：
   - 更新预约状态为"已服务"
   - 扣减相关库存
   - **自动创建服务执行记录**，填充以下信息：
     - 会员信息（从预约记录获取）
     - 服务项目信息（从预约记录获取）
     - 员工信息（从预约记录获取）
     - 服务日期（使用预约日期）
     - 费用（从服务项目数据库读取最新价格）

#### 幂等性保证：

为避免重复插入，系统实现了幂等性控制：

- 每条服务执行记录关联一个预约ID（appointmentId）
- 在创建记录前，系统会检查是否已存在该预约ID的执行记录
- 如果已存在，则不会重复创建，确保同一预约记录不会生成多条执行记录
- 即使多次点击"已服务"按钮或重复提交，也只会创建一条执行记录

## 技术实现

### 后端实现

#### 1. 实体类 (Entity)
- **文件**: `src/main/java/com/barbershop/system/entity/ServiceExecution.java`
- **说明**: JPA实体，映射数据库表 `service_execution`

#### 2. 数据访问层 (Repository)
- **文件**: `src/main/java/com/barbershop/system/repository/ServiceExecutionRepository.java`
- **说明**: 提供数据库操作接口，包括：
  - 基础CRUD操作
  - 根据预约ID查询（幂等性检查）
  - 多条件组合查询

#### 3. 业务逻辑层 (Service)
- **文件**: `src/main/java/com/barbershop/system/service/ServiceExecutionService.java`
- **说明**: 实现业务逻辑，包括：
  - 查询所有记录
  - 多条件筛选查询
  - 新增记录（验证会员/服务/员工是否存在）
  - 修改记录（自动更新关联信息和费用）
  - 删除记录
  - 从预约创建记录（带幂等性控制）

#### 4. 控制器 (Controller)
- **文件**: `src/main/java/com/barbershop/system/controller/ServiceExecutionController.java`
- **说明**: REST API端点，提供：
  - `GET /api/service-executions` - 获取所有记录
  - `GET /api/service-executions/search?memberId=&serviceId=&employeeId=&serviceDate=` - 条件查询
  - `POST /api/service-executions` - 新增记录
  - `PUT /api/service-executions/{id}` - 修改记录
  - `DELETE /api/service-executions/{id}` - 删除记录

#### 5. 预约服务修改
- **文件**: `src/main/java/com/barbershop/system/service/AppointmentService.java`
- **修改点**: 在 `complete()` 方法中添加自动创建服务执行记录的逻辑

### 前端实现

#### 1. 页面布局
- **文件**: `src/main/resources/static/index.html`
- **新增内容**:
  - 侧边栏菜单项："✅ 服务执行管理"
  - 服务执行管理页面区域
  - 新增服务执行记录弹窗
  - 修改服务执行记录弹窗

#### 2. JavaScript功能
- **文件**: `src/main/resources/static/index.html` (script区域)
- **新增函数**:
  - `loadExecutions()` - 加载服务执行记录列表
  - `renderExecutionTable(data)` - 渲染表格
  - `searchExecution()` - 执行筛选查询
  - `resetExecutionSearch()` - 重置筛选条件
  - `openAddExecutionModal()` - 打开新增弹窗
  - `submitExecution()` - 提交新增
  - `openEditExecutionModal(id)` - 打开修改弹窗
  - `submitEditExecution()` - 提交修改
  - `delExecution(id)` - 删除记录

## 数据库表结构

系统使用JPA的`ddl-auto=update`模式，会自动创建表结构。表结构如下：

```sql
CREATE TABLE service_execution (
    id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT,
    member_name VARCHAR(255),
    service_id INT,
    service_name VARCHAR(255),
    employee_id INT,
    employee_name VARCHAR(255),
    cost DECIMAL(19,2),
    service_date DATE,
    appointment_id INT,
    UNIQUE KEY uk_appointment_id (appointment_id)
);
```

**注意**: `appointment_id` 字段有唯一索引，确保同一预约不会创建多条执行记录。

## 使用指南

### 启动应用

1. 确保MySQL数据库运行在 `localhost:3306`
2. 创建数据库：
   ```sql
   CREATE DATABASE barbershop_mis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
3. 在 `application.properties` 中配置数据库密码
4. 运行应用：
   ```bash
   mvn spring-boot:run
   ```
5. 访问：http://localhost:8080

### 使用流程示例

#### 场景1：手动记录服务执行

1. 登录系统
2. 点击左侧菜单"✅ 服务执行管理"
3. 点击"+ 新增服务执行记录"
4. 选择会员、服务项目、员工，设置日期
5. 点击"保存"

#### 场景2：通过预约自动创建

1. 在"📅 预约管理"中创建预约
2. 当服务完成时，点击预约的"✅ 已服务"按钮
3. 系统自动创建服务执行记录
4. 切换到"✅ 服务执行管理"可查看自动创建的记录

#### 场景3：查询特定会员的服务记录

1. 在"✅ 服务执行管理"页面
2. 在"筛选条件"区域，从"全部会员"下拉列表选择目标会员
3. 点击"🔍 查询"
4. 查看该会员的所有服务执行记录

#### 场景4：查询特定日期的服务记录

1. 在"✅ 服务执行管理"页面
2. 选择日期
3. 点击"🔍 查询"
4. 查看该日期的所有服务执行记录

#### 场景5：组合查询

1. 同时选择会员、服务项目、员工和日期
2. 点击"🔍 查询"
3. 系统返回满足所有条件的记录

## 注意事项

1. **数据一致性**: 修改服务项目时，费用会自动更新为新的服务项目价格
2. **关联验证**: 新增或修改时，系统会验证会员、服务项目、员工是否存在
3. **幂等性**: 同一预约记录只能创建一条服务执行记录，避免重复
4. **删除操作**: 删除服务执行记录不会影响原预约记录
5. **日期默认值**: 新增记录时，如果不选择日期，系统默认使用当天日期

## 错误处理

系统提供友好的错误提示：

- ✅ 操作成功时显示绿色成功提示
- ❌ 操作失败时显示红色错误提示
- 必填项验证：提交前检查必填字段
- 网络错误提示：网络异常时给出明确提示

## 后续扩展建议

1. **统计报表**: 基于服务执行记录生成营业报表
2. **费用结算**: 与会员余额系统集成，自动扣费
3. **员工业绩**: 统计员工服务次数和收入
4. **服务评价**: 允许会员对服务进行评价
5. **数据导出**: 支持将服务执行记录导出为Excel
6. **消息通知**: 服务完成后发送通知给会员

## 维护说明

### 日常维护

- 定期备份 `service_execution` 表数据
- 监控表数据增长，必要时进行归档

### 故障排查

1. **记录创建失败**: 检查会员/服务/员工是否存在
2. **重复记录**: 检查 `appointment_id` 唯一索引是否正常
3. **费用不正确**: 检查服务项目的价格设置

---

**版本**: 1.0  
**最后更新**: 2026-01-08  
**维护者**: 理发店管理系统开发团队
