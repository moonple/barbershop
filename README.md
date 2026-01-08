# 理发店管理系统

悦容理发店管理系统 - 基于Spring Boot的理发店综合管理平台

## 系统简介

本系统是一个完整的理发店管理系统，提供会员管理、员工管理、服务项目管理、预约管理、库存管理、服务执行管理等功能模块。

## 技术栈

- **后端**: Spring Boot 3.4.1, Spring Data JPA
- **数据库**: MySQL 8.0+
- **前端**: HTML5, JavaScript (原生), CSS3
- **构建工具**: Maven
- **Java版本**: 17

## 功能模块

### 1. 会员管理 👤
- 新增、修改、删除会员信息
- 会员信息包括：姓名、手机号、性别、余额
- 按手机号查询会员
- 会员注册时间自动记录

### 2. 员工管理 👔
- 新增、修改、删除员工信息
- 员工信息包括：工号(ID)、姓名、手机号、性别
- 按工号或手机号查询员工

### 3. 管理员管理 🛡️
- 新增、修改、删除管理员账户
- 管理员信息包括：姓名、登录手机号、性别
- 管理员登录认证

### 4. 库存管理 📦
- 新增、修改、删除库存物品
- 物品信息包括：名称、当前库存、警戒线、备注状态
- 库存不足自动提醒
- 按物品名称或ID查询
- **注意**：采购功能已迁移到财务管理模块

### 5. 服务项目管理 💇
- 新增、修改、删除服务项目
- 服务信息包括：名称、价格、服务时长、消耗物品
- 服务可关联最多5个消耗物品
- 按服务名称或ID查询

### 6. 预约管理 📅
- 新增预约（选择日期、时间、会员、服务、员工）
- 预约状态管理：待服务、已服务、已取消
- 营业时间校验（9:00-12:00, 14:00-17:00）
- 员工时间冲突检测
- 完成预约时自动扣减库存
- 按日期或关键词查询预约

### 7. 服务执行管理 ✅
- 记录每次服务的实际执行情况
- 执行记录包括：会员、服务项目、员工、费用、日期
- 新增、修改、删除执行记录
- 多条件筛选查询（会员、服务、员工、日期）
- **自动创建**：当预约状态变为"已服务"时自动创建执行记录
- **幂等性保证**：同一预约不会创建重复执行记录

详细文档请参考：[服务执行管理模块文档](SERVICE_EXECUTION_MODULE.md)

### 8. 财务管理 💰 (新增)

#### 8.1 服务收入 📈
- 展示所有服务执行记录作为服务收入来源
- 记录字段：服务记录ID、会员ID、项目ID、员工ID、费用、日期
- 多条件筛选：支持按会员、服务项目、员工、日期进行筛选
- **总收入统计**：自动计算当前筛选条件下的全部记录总和（全量汇总，非当前页）
- 数据来源：复用服务执行管理模块的数据

#### 8.2 采购支出 📉
- 管理物品采购记录和采购历史
- 采购功能：选择物品、填写数量和单价后提交
  - 自动创建采购记录
  - 自动更新对应物品的库存（库存 = 库存 + 采购数量）
  - 支持事务性处理，保证数据一致性
- 记录字段：采购记录ID、物品ID、物品名称、数量、单价、总价、日期
- 多条件筛选：支持按物品、日期进行筛选
- **总支出统计**：自动计算当前筛选条件下的全部采购记录总和（全量汇总，非当前页）
- **注意**：此功能从库存管理模块迁移而来，库存管理模块不再提供采购入口

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE barbershop_mis CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改 `src/main/resources/application.properties` 配置文件：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/barbershop_mis?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false
spring.datasource.username=root
spring.datasource.password=你的数据库密码
```

### 启动应用

1. 编译项目：
```bash
mvn clean compile
```

2. 运行应用：
```bash
mvn spring-boot:run
```

3. 访问系统：
```
http://localhost:8080/login.html
```

### 初始化数据

系统使用JPA的`ddl-auto=update`模式，首次运行时会自动创建所有数据表：
- `member` - 会员表
- `employee` - 员工表
- `admin` - 管理员表
- `inventory` - 库存表
- `service_item` - 服务项目表
- `appointment` - 预约表
- `service_execution` - 服务执行记录表
- `financial_record` - 财务记录表
- `purchase_record` - 采购记录表

需要手动创建初始管理员账户：
```sql
INSERT INTO admin (name, phone, gender) VALUES ('管理员', '13800138000', '男');
```

然后使用手机号 `13800138000` 登录系统。

## 项目结构

```
barbershop/
├── src/
│   ├── main/
│   │   ├── java/com/barbershop/system/
│   │   │   ├── SystemApplication.java          # 主启动类
│   │   │   ├── controller/                     # 控制器层
│   │   │   │   ├── MemberController.java
│   │   │   │   ├── EmployeeController.java
│   │   │   │   ├── AdminController.java
│   │   │   │   ├── InventoryController.java
│   │   │   │   ├── ServiceItemController.java
│   │   │   │   ├── AppointmentController.java
│   │   │   │   ├── ServiceExecutionController.java  # 服务执行管理
│   │   │   │   └── FinancialController.java         # 财务管理
│   │   │   ├── entity/                         # 实体类
│   │   │   │   ├── Member.java
│   │   │   │   ├── Employee.java
│   │   │   │   ├── Admin.java
│   │   │   │   ├── Inventory.java
│   │   │   │   ├── ServiceItem.java
│   │   │   │   ├── Appointment.java
│   │   │   │   ├── FinancialRecord.java
│   │   │   │   ├── ServiceExecution.java       # 服务执行实体
│   │   │   │   └── PurchaseRecord.java         # 采购记录实体
│   │   │   ├── repository/                     # 数据访问层
│   │   │   │   ├── MemberRepository.java
│   │   │   │   ├── EmployeeRepository.java
│   │   │   │   ├── AdminRepository.java
│   │   │   │   ├── InventoryRepository.java
│   │   │   │   ├── ServiceItemRepository.java
│   │   │   │   ├── AppointmentRepository.java
│   │   │   │   ├── FinancialRecordRepository.java
│   │   │   │   ├── ServiceExecutionRepository.java  # 服务执行数据访问
│   │   │   │   └── PurchaseRecordRepository.java    # 采购记录数据访问
│   │   │   └── service/                        # 业务逻辑层
│   │   │       ├── MemberService.java
│   │   │       ├── InventoryService.java
│   │   │       ├── AppointmentService.java
│   │   │       └── ServiceExecutionService.java     # 服务执行业务逻辑
│   │   └── resources/
│   │       ├── application.properties          # 配置文件
│   │       └── static/                         # 静态资源
│   │           ├── index.html                  # 主页面
│   │           └── login.html                  # 登录页面
│   └── test/
│       └── java/com/barbershop/system/
│           └── SystemApplicationTests.java     # 测试类
├── pom.xml                                     # Maven配置
├── README.md                                   # 项目说明
└── SERVICE_EXECUTION_MODULE.md                 # 服务执行模块文档
```

## API接口文档

### 服务执行管理 API

#### 获取所有执行记录
```
GET /api/service-executions
Response: List<ServiceExecution>
```

#### 条件查询
```
GET /api/service-executions/search?memberId=1&serviceId=2&employeeId=3&serviceDate=2026-01-08
Parameters:
  - memberId: 会员ID (可选)
  - serviceId: 服务项目ID (可选)
  - employeeId: 员工ID (可选)
  - serviceDate: 服务日期 (可选, 格式: YYYY-MM-DD)
Response: List<ServiceExecution>
```

#### 新增执行记录
```
POST /api/service-executions
Request Body:
{
  "memberId": 1,
  "serviceId": 2,
  "employeeId": 3,
  "serviceDate": "2026-01-08"
}
Response: ServiceExecution
```

#### 修改执行记录
```
PUT /api/service-executions/{id}
Request Body:
{
  "memberId": 1,
  "serviceId": 2,
  "employeeId": 3,
  "serviceDate": "2026-01-08"
}
Response: ServiceExecution
```

#### 删除执行记录
```
DELETE /api/service-executions/{id}
Response: 200 OK
```

### 财务管理 API

#### 查询服务收入
```
GET /api/financial/income?memberId=1&serviceId=2&employeeId=3&serviceDate=2026-01-08
Parameters:
  - memberId: 会员ID (可选)
  - serviceId: 服务项目ID (可选)
  - employeeId: 员工ID (可选)
  - serviceDate: 服务日期 (可选, 格式: YYYY-MM-DD)
Response:
{
  "records": [List<ServiceExecution>],
  "totalIncome": BigDecimal  // 当前筛选条件下的总收入
}
```

#### 查询采购记录
```
GET /api/financial/purchase?inventoryId=1&purchaseDate=2026-01-08
Parameters:
  - inventoryId: 物品ID (可选)
  - purchaseDate: 采购日期 (可选, 格式: YYYY-MM-DD)
Response:
{
  "records": [List<PurchaseRecord>],
  "totalExpenditure": BigDecimal  // 当前筛选条件下的总支出
}
```

#### 新增采购记录
```
POST /api/financial/purchase
Request Body:
{
  "inventoryId": 1,
  "quantity": 10,
  "unitPrice": 15.50
}
Response: PurchaseRecord
说明：自动更新对应物品的库存数量，事务性处理
```

#### 删除采购记录
```
DELETE /api/financial/purchase/{id}
Response: 200 OK
说明：删除记录不会回退库存
```

其他模块的API接口请参考各Controller类的实现。

## 业务流程

### 完整服务流程

1. **会员注册**：在会员管理模块新增会员
2. **预约创建**：在预约管理模块创建预约，选择会员、服务、员工和时间
3. **服务执行**：当服务完成时，点击预约的"已服务"按钮
4. **自动记录**：系统自动创建服务执行记录并扣减库存
5. **查询统计**：
   - 在服务执行管理模块查看服务历史记录
   - 在财务管理模块的"服务收入"查看收入统计

### 采购管理流程

1. **查看库存**：在库存管理模块查看当前库存状态
2. **采购入库**：在财务管理模块的"采购支出"中新增采购记录
3. **自动更新**：系统自动更新物品库存并记录采购历史
4. **统计查询**：在采购支出页面查看采购历史和支出统计

## 开发说明

### 添加新模块的步骤

1. 创建Entity类（`entity/`目录）
2. 创建Repository接口（`repository/`目录）
3. 创建Service类（`service/`目录）
4. 创建Controller类（`controller/`目录）
5. 在`index.html`中添加前端页面和JavaScript

### 代码规范

- Entity类使用Lombok的`@Data`注解
- Repository继承`JpaRepository`
- Service类添加`@Service`注解，方法添加`@Transactional`
- Controller类添加`@RestController`和`@RequestMapping`
- 前端JavaScript函数使用驼峰命名

## 注意事项

1. **数据库编码**：确保数据库使用utf8mb4编码，支持中文和emoji
2. **营业时间**：系统默认营业时间为9:00-17:00，12:00-14:00午休
3. **库存管理**：服务执行时会自动扣减关联的库存物品
4. **幂等性**：预约完成时自动创建的执行记录有幂等性保证，不会重复
5. **数据备份**：定期备份数据库，避免数据丢失
6. **采购管理**：采购功能已从库存管理迁移至财务管理模块，库存管理模块不再提供采购入口

## 常见问题

### Q: 无法连接数据库？
A: 检查MySQL是否运行，数据库名称和密码是否正确。

### Q: 预约时提示时间冲突？
A: 该员工在该时间段已有预约，请选择其他时间或员工。

### Q: 如何进行采购入库？
A: 在财务管理模块的"采购支出"页面点击"新增采购记录"，选择物品、填写数量和单价后提交即可。

### Q: 财务统计是否包含所有记录？
A: 是的。服务收入和采购支出的统计数据都是基于当前筛选条件下的全部记录，不限于当前页面显示的数据。

### Q: 同一预约点击多次"已服务"会创建多条执行记录吗？
A: 不会。系统有幂等性保证，同一预约只会创建一条执行记录。

### Q: 删除采购记录会恢复库存吗？
A: 不会。删除采购记录仅删除记录本身，不会回退已经增加的库存数量。

## 版本历史

- **v1.1** (2026-01-08)
  - 新增财务管理模块
  - 服务收入：基于服务执行记录的收入统计
  - 采购支出：采购记录管理和支出统计
  - 采购功能从库存管理迁移至财务模块
  - 支持全量数据统计（不受分页限制）

- **v1.0** (2026-01-08)
  - 新增服务执行管理模块
  - 实现预约与执行记录的自动关联
  - 支持多条件筛选查询
  - 幂等性保证避免重复记录

## 许可证

本项目为理发店内部管理系统。

## 联系方式

如有问题或建议，请联系开发团队。

---

**项目维护者**: 悦容理发店管理系统开发团队  
**最后更新**: 2026-01-08
