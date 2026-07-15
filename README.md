# 测试平台（Spring Boot 电商后端）

## 技术栈
- Spring Boot 3.2.6
- MyBatis + MySQL
- JWT + BCrypt
- Knife4j 接口文档
- JUnit 5 + Mockito 单元测试

## 核心功能
- 用户管理：注册、登录、JWT 认证、密码加密
- 商品管理：CRUD、分页搜索、逻辑删除、库存扣减
- 订单管理：下单、支付、取消（退款）、状态机

## 运行项目
1. 创建 MySQL 数据库 `test_platform`
2. 修改 `application.properties` 中的数据库密码
3. 运行 `TestPlatformApplication.java`
4. 访问 `http://localhost:8080/doc.html` 查看接口文档

## 测试
- 单元测试覆盖率 85%+
- 运行 `mvn test` 执行所有测试
