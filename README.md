# 智行云

> 智行云火车票售票平台 — 基于 Spring Boot 3 微服务的前后端分离系统，覆盖会员登录、余票查询、选座购票、支付扣款与定时任务。针对高峰购票场景，实现了 **防超卖** 与 **削峰**。

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.0-green)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2022.0-blue)](https://spring.io/projects/spring-cloud)
[![Vue](https://img.shields.io/badge/Vue-3-brightgreen)](https://vuejs.org/)
[![MySQL](https://img.shields.io/badge/MySQL-8-blue)](https://www.mysql.com/)

---

## 项目介绍

智行云模拟 12306 核心购票链路：用户在用户端登录、维护乘车人、查票下单；运营在管理端维护车站/车次，并生成每日余票。请求统一经 API 网关进入，会员、业务、支付、批处理拆成独立服务。

适合作为微服务、高并发购票与分布式事务的实践项目。

### 能做什么

| 端 | 能力 |
|----|------|
| **用户端** `:9000` | 验证码登录、乘车人管理、余票查询、选座购票、订单查询 |
| **管理端** `:9001` | 车站/车次/车厢/座位配置、每日车次生成、订单查看、Quartz 任务管理 |

---

## 项目亮点

### 1. 防超卖

购票不是「先查库再扣库存」。下单时先用 **Redisson 分布式锁** 锁住车次区间，再用 **Redis + Lua** 原子扣减余票；座位售卖状态用位图标记站区间，避免同一座位被重复卖出。库存不足立即失败，不把超卖请求打进数据库。

### 2. 削峰

网关对购票接口做 **令牌桶限流**（默认容量 20、每秒补充 5，超限返回 429）。业务侧可将下单改为 **RabbitMQ 异步入队**：接口只负责接单，消费者再执行锁库存、占座、Seata 支付，把瞬时高峰摊开。

### 3. 分布式事务

占座与扣款跨 `business`、`payment` 两个库，使用 **Seata AT** 全局事务；失败回滚座位与余额，避免「票扣了钱没扣」或反过来。

### 4. 微服务拆分

Gateway 只做路由、JWT 鉴权、限流；会员、车次订单、支付、定时任务分库分服务，通过 OpenFeign 调用。

### 5. 每日数据自动化

管理端配置车次模板后，`batch` 用 **Quartz** 按日生成车次、车厢、座位和余票，并处理超时未支付订单。

---

## 技术栈

| 层次 | 技术 |
|------|------|
| 后端 | Java 17、Spring Boot 3.0、Spring Cloud Gateway、OpenFeign、MyBatis、MySQL 8 |
| 并发与中间件 | Redis / Redisson、RabbitMQ、Seata 1.7、Quartz |
| 前端 | Vue 3、Vue Router、Vuex、Ant Design Vue |
| 运行 | Maven Wrapper、Docker Compose |

---

## 系统架构

```text
用户端 :9000                 管理端 :9001
        \                       /
         \                     /
          v                   v
        智行云 Gateway :8000
        路由 / JWT / 令牌桶限流
              |
    +---------+---------+---------+
    |         |         |         |
 member    business  payment    batch
  :8001      :8002     :8004     :8003
 会员/乘车人  余票/订单   账户扣款   Quartz
    |         |         |         |
 train_member train_business train_payment train_batch
              |
         Redis / RabbitMQ / Seata
```

购票主路径：`Gateway 限流` → `business 入队或同步下单` → `Redis 锁 + Lua 扣库存` → `选座写位图` → `Seata 调 payment 扣款`。

---

## 模块与端口

| 模块 | 端口 | 说明 |
|------|------|------|
| gateway | 8000 | 智行云 API 网关 |
| member | 8001 | 会员、乘车人 |
| business | 8002 | 车次、余票、购票 |
| batch | 8003 | 定时任务 |
| payment | 8004 | 支付账户 |
| web | 9000 | 智行云用户端 |
| admin | 9001 | 智行云管理端 |

网关路由：`/member/**`、`/business/**`、`/batch/**`、`/payment/**`。

---

## 详细部署教程

### 一、环境准备

| 软件 | 版本 | 说明 |
|------|------|------|
| JDK | **17** | Spring Boot 3 最低要求，请配置 `JAVA_HOME` |
| Maven | 3.6+ | 也可用仓库自带 `mvnw` / `mvnw.cmd` |
| MySQL | 8.x | 端口 `3306`，下文默认账号 `root` / `root` |
| Node.js | 16+ | 前端构建 |
| Docker Desktop | 近期版本 | 启动 Redis、RabbitMQ、Seata |
| IDE（可选） | IntelliJ IDEA | 打开本仓库根目录（含 `pom.xml` 的目录） |

克隆：

```bash
git clone https://github.com/boyuan623-coder/zhixingyun-platform.git
cd zhixingyun-platform
```

若数据库账号不是 `root`/`root`，请修改：

- `member/src/main/resources/application.properties`
- `business/src/main/resources/application.properties`
- `batch/src/main/resources/application.properties`
- `payment/src/main/resources/application.properties`

也可复制各模块的 `application-local.properties.example` 为 `application-local.properties`（该文件不会提交到 Git），再自行增加 `spring.profiles.active=local`。

### 二、启动中间件

在项目根目录：

```bash
cd docker
docker compose up -d
cd ..
```

启动后应看到三个容器：

| 容器 | 端口 | 用途 |
|------|------|------|
| train-redis | 6379 | 库存、分布式锁、网关限流 |
| train-rabbitmq | 5672 / 15672 | 异步下单削峰；控制台用户 `train` / `train123` |
| train-seata | 8091 / 7091 | 分布式事务 TC |

本机若已占用 6379，可停掉本地 Redis，或改 compose 与 `application.properties` 中的端口。

查看状态：

```bash
docker ps
```

RabbitMQ 管理台：http://localhost:15672

### 三、初始化数据库

MySQL 需允许本地登录。在项目根目录执行（把 `root` 换成你的账号）：

**Windows PowerShell：**

```powershell
Get-Content sql/full-dump.sql -Raw | mysql -u root -proot
Get-Content sql/payment.sql -Raw | mysql -u root -proot
Get-Content sql/seata_undo_log.sql -Raw | mysql -u root -proot
Get-Content sql/alter_confirm_order_amount.sql -Raw | mysql -u root -proot
```

若 `alter_confirm_order_amount.sql` 提示列已存在，可忽略。

**Linux / macOS：**

```bash
mysql -u root -proot < sql/full-dump.sql
mysql -u root -proot < sql/payment.sql
mysql -u root -proot < sql/seata_undo_log.sql
mysql -u root -proot < sql/alter_confirm_order_amount.sql
```

将创建并写入：

| 库 | 内容 |
|----|------|
| `train_member` | 会员、乘车人 |
| `train_business` | 车站/车次/余票/订单、Seata undo_log |
| `train_batch` | Quartz 表 |
| `train_payment` | 支付账户、流水、undo_log |

`full-dump.sql` 已含演示车站、车次和约 15 天每日余票。演示会员手机号：`13000000001` ~ `13000000010`。

### 四、编译后端

**Windows：**

```bat
mvnw.cmd clean install -DskipTests
```

**Linux / macOS：**

```bash
chmod +x mvnw
./mvnw clean install -DskipTests
```

成功后 `common` 会先被安装到本地仓库，再编译各业务模块。

### 五、启动后端

用 IDEA 打开项目根目录，等待 Maven 导入完成后，分别运行：

| 顺序 | 主类 | 端口 |
|------|------|------|
| 1 | `com.jiawa.train.member.config.MemberApplication` | 8001 |
| 2 | `com.jiawa.train.business.config.BusinessApplication` | 8002 |
| 3 | `com.jiawa.train.payment.config.PaymentApplication` | 8004 |
| 4 | `com.jiawa.train.batch.config.BatchApplication` | 8003 |
| 5 | `com.jiawa.train.gateway.config.GatewayApplication` | 8000 |

1～4 可并行；**网关最后启动**，否则会 502。

日志出现「启动成功」且能访问健康检查即可：

```text
http://127.0.0.1:8001/member/hello
http://127.0.0.1:8002/business/hello
http://127.0.0.1:8000/member/hello
```

### 六、启动前端

在项目根目录：

```bash
npm install
npm run install:all
npm run dev
```

将同时拉起：

- 智行云用户端 http://localhost:9000
- 智行云管理端 http://localhost:9001

只启动一端：

```bash
npm run dev:web
npm run dev:admin
```

### 七、验证是否部署成功

1. 打开 http://localhost:9000
2. 手机号 `13000000001`，验证码 `8888` 登录
3. 查询余票（种子数据日期约在 `2025-07-07` ~ `2025-07-21`）
4. 选择车次与乘车人下单
5. 管理端 http://localhost:9001 查看车站、每日车次、订单

前端默认请求网关 `http://127.0.0.1:8000`。

---

## 常见问题

**IDEA 没有绿色运行按钮**  
请打开带 `pom.xml` 的项目根目录，而不是外层 `train-master` 文件夹，并等待 Maven 导入完成。确认 SDK 为 JDK 17。

**网关 502**  
member / business / payment / batch 未全部启动，或端口被占用。

**查不到车次**  
确认已导入 `full-dump.sql`，查询日期落在种子数据范围内。也可在管理端对某日执行「生成每日车次」。

**Redis / 限流 / 超卖相关异常**  
确认 `docker compose` 中 Redis、RabbitMQ、Seata 已启动，端口与配置一致。

**Seata 报错或支付不回滚**  
确认 Seata Server `:8091` 可用，且已执行 `seata_undo_log.sql`、`payment.sql`。

**JDK 版本错误**  
必须使用 JDK 17+。

---

## 开发说明

- 开发环境验证码固定为 `8888`，无需真实短信。
- 购票异步开关：`business` 配置 `train.confirm.async`（`true` 走 MQ 削峰）。
- 网关限流：`train.gateway.rate-limit.*`。
- 本地覆盖配置请用 `application-local.properties`，不要把真实口令和内网地址提交到 Git。

本项目仅供学习交流。
