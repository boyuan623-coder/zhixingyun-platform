# 智行云

智行云火车票售票系统。Spring Boot 3 + Spring Cloud Gateway / OpenFeign + MyBatis + MySQL + Redis + Seata + Quartz。

## 环境

JDK 17、Maven 3.6+、MySQL 8、Node.js 16+、Docker（Redis / RabbitMQ / Seata）

## 启动

```bash
# 1. 中间件
cd docker && docker compose up -d && cd ..

# 2. 数据库（默认账号 root / root）
mysql -u root -p < sql/full-dump.sql
mysql -u root -p < sql/payment.sql
mysql -u root -p < sql/seata_undo_log.sql
mysql -u root -p < sql/alter_confirm_order_amount.sql

# 3. 后端
./mvnw clean install -DskipTests
```

按顺序启动：`member` 8001 → `business` 8002 → `payment` 8004 → `batch` 8003 → `gateway` 8000。

```bash
# 4. 前端
npm install
npm run install:all
npm run dev
```

| 入口 | 地址 |
|------|------|
| 智行云用户端 | http://localhost:9000 |
| 智行云管理端 | http://localhost:9001 |
| 智行云网关 | http://localhost:8000 |

登录：手机号 `13000000001`，验证码 `8888`。
