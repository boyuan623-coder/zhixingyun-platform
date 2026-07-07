# 智行云平台

智行云火车票售票系统 — 前后端分离的微服务架构项目。

## 技术栈

- **后端**：Spring Boot 3、Spring Cloud Gateway、MyBatis、MySQL
- **前端**：Vue 3、Ant Design Vue
- **批处理**：Quartz 定时任务

## 模块说明

| 模块 | 端口 | 说明 |
|------|------|------|
| gateway | 8000 | API 网关 |
| member | 8001 | 会员服务 |
| business | 8002 | 业务服务（车次、订票） |
| batch | 8003 | 批处理服务 |
| web | 9000 | 用户端 |
| admin | 9001 | 管理端 |

## 环境要求

- JDK 17
- Maven 3.x
- MySQL 8.x
- Node.js 16+

## 数据库初始化

```bash
# 1. 创建库并导入表结构
mysql -u root -p < sql/member.sql
mysql -u root -p < sql/business.sql
mysql -u root -p < sql/batch.sql

# 2. 导入测试数据（车站、车次、会员等）
mysql -u root -p < sql/seed-data.sql

# 或一键导入完整数据库备份
mysql -u root -p < sql/full-dump.sql
```

本地数据库默认配置：`root` / `root`，见各模块 `application-local.properties`。

## 启动顺序

1. MySQL
2. member → business → batch（可并行）
3. gateway
4. 前端：`npm install && npm run install:all && npm run dev`

## 测试账号

- 手机号：`13000000001` ~ `13000000010`
- 验证码：`8888`
