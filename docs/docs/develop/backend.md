# 后端开发文档

本文档面向需要二次开发 HOJ 后端的开发者，介绍后端的项目结构、开发环境准备与本地启动方式。

## 一、技术栈与项目结构

### 1. 技术栈

后端基于 **Spring Boot + Spring Cloud Alibaba** 的微服务架构，主要技术选型如下：

| 分类       | 技术                                    | 说明                                   |
| ---------- | --------------------------------------- | -------------------------------------- |
| 主体框架   | Spring Boot 2.2.x                       | Web 服务基础框架                       |
| 微服务     | Spring Cloud Alibaba / Nacos            | 服务注册中心 + 配置中心（配置动态刷新） |
| ORM        | MyBatis-Plus                           | 数据实体与数据库数据的转化              |
| 数据库     | MySQL 8.0                              | 业务数据存储                           |
| 缓存/队列  | Redis 5.x                              | 数据缓存 + 等待评测队列（list）        |
| 安全框架   | Shiro                                  | 用户角色权限管理、token 刷新            |
| 判题沙盒   | Go-Judge                               | cgroup 隔离的安全沙盒                  |
| 工程增强   | Lombok                                 | 减少样板代码                           |

后端共两个可运行服务：

- **hoj-backend（数据服务，`DataBackup` 模块）**：处理所有业务逻辑，对外提供 REST API，并将评测任务放入 Redis 队列、调度判题服务。
- **hoj-judgeserver（评测服务，`JudgeServer` 模块）**：接收评测任务，调用 Go-Judge 沙盒编译运行用户代码，把评测结果写回数据库；同时负责远程 OJ（vjudge）的爬虫提交与轮询。

### 2. 项目结构

```text
hoj-springboot/
├── pom.xml                 # 父工程（Maven 多模块聚合）
├── api/                    # 公共模块：被 DataBackup 与 JudgeServer 共享
│   └── src/main/java/top/hcode/hoj/pojo/
│       ├── entity/         # 数据库实体
│       └── dto/            # 跨服务传输对象
├── DataBackup/             # hoj-backend 数据服务
│   └── src/main/java/top/hcode/hoj/
│       ├── controller/     # 控制层（admin / file / group / msg / oj）
│       ├── service/        # 服务层接口 + impl 实现
│       ├── manager/        # 业务编排层（跨多个 service 的组合逻辑）
│       ├── dao/            # 数据访问层（assignment / contest / discussion / judge / ...）
│       ├── mapper/         # MyBatis-Plus Mapper 接口 + xml
│       ├── pojo/           # bo / dto / vo / entity 数据对象
│       ├── config/         # 配置类（WebMvc、Redis、Shiro 等）
│       ├── shiro/          # 认证授权相关（Realm、过滤器）
│       ├── interceptor/    # 拦截器
│       ├── annotation/     # 自定义注解（如权限校验）
│       ├── advice/         # 全局异常处理
│       ├── schedule/       # 定时任务
│       ├── crawler/        # 远程 OJ 爬虫（language / problem）
│       ├── judge/          # 评测相关（self / remote）
│       ├── common/         # 公共常量、结果集、异常
│       └── utils/          # 工具类
└── JudgeServer/            # hoj-judgeserver 评测服务
    └── src/main/java/top/hcode/hoj/
        ├── controller/     # 对外评测接口
        ├── judge/          # 评测核心（task / entity）
        ├── remoteJudge/    # 远程评测核心
        ├── service/        # 服务层 + impl
        ├── dao/            # 数据访问层
        ├── mapper/         # Mapper + xml
        └── util/           # 工具类
```

### 3. 分层约定

代码采用 **Controller → Service → Manager → DAO/Mapper** 的分层结构：

- **Controller**：接收 HTTP 请求，参数校验，调用 Service/Manager，返回统一结果集。
- **Service**：业务逻辑，按业务域拆分（用户、题目、比赛、训练、讨论、团队、消息等）。
- **Manager**：跨模块的业务编排，避免 Controller 直接拼装多个 Service 调用。
- **DAO / Mapper**：数据库访问；DAO 提供面向业务的封装，Mapper 是 MyBatis-Plus 的基础 CRUD。

## 二、环境准备

本地二次开发需要以下基础环境，建议与部署环境保持一致：

| 依赖   | 版本      | 作用                           |
| ------ | --------- | ------------------------------ |
| JDK    | 1.8       | 后端运行环境                   |
| Maven  | 3.x       | 构建与依赖管理                 |
| MySQL  | 8.0       | 业务数据库                     |
| Redis  | 5.x       | 缓存与评测队列                 |
| Nacos  | 1.4.x     | 服务注册中心 + 配置中心        |

> 快速搭好 MySQL / Redis / Nacos 可参考 [单体部署](/monomer/mysql/) 系列文档，或直接使用 [快速部署](/deploy/docker/) 的 docker-compose 一键拉起整套依赖。

### 1. 初始化数据库

后端依赖两个库：`hoj`（业务库）和 `nacos`（Nacos 配置库）。初始化脚本位于仓库的 `sqlAndsetting/` 目录：

```bash
# hoj 业务库结构 + 基础数据
sqlAndsetting/hoj.sql

# 后续增量更新脚本（按时间顺序执行）
sqlAndsetting/hoj-update.sql
sqlAndsetting/hoj-assignment-update.sql
sqlAndsetting/hoj-pk-chat-update.sql

# Nacos 配置库结构
sqlAndsetting/nacos.sql
```

将以上脚本依次导入对应数据库即可。Nacos 若使用自身内嵌库可忽略 `nacos.sql`。

### 2. 准备 Nacos 配置

后端配置通过 Nacos 配置中心下发，`bootstrap.yml` 中的关键项如下：

```yaml
nacos-url: ${NACOS_URL:127.0.0.1:8848}
nacos-username: ${NACOS_USERNAME:root}
nacos-password: ${NACOS_PASSWORD:hoj123456}

spring:
  profiles:
    active: prod            # 生效的配置为 hoj-prod.yml
  application:
    name: hoj-data-backup
  cloud:
    nacos:
      discovery:
        server-addr: ${nacos-url}   # 服务注册中心地址
      config:
        server-addr: ${nacos-url}   # 配置中心地址
        file-extension: yml
        prefix: hoj                 # 加载 hoj-<profile>.yml
```

因此，本地启动前需要在 Nacos 控制台（`http://127.0.0.1:8848/nacos`）上传对应的配置项，至少包括：

- `hoj-prod.yml`（或 `hoj-dev.yml`，与 `spring.profiles.active` 对应）：数据库连接、Redis、JWT 密钥、邮件、远程判题账号等。
- `hoj-switch.yml`、`hoj-web.yml`：功能开关与网站配置（`bootstrap.yml` 中声明）。

> 配置项与 `.env` / `docker-compose.yml` 中的环境变量一一对应，可参考 [快速部署](/deploy/docker/) 中的参数说明逐一填写。

## 三、本地启动

### 1. 构建打包

在 `hoj-springboot` 目录下执行 Maven 打包：

```bash
cd hoj-springboot
mvn clean package -Dmaven.test.skip=true
```

打包产物：

- `DataBackup/target/` 下的后端 jar（数据服务）
- `JudgeServer/target/` 下的评测服务 jar

### 2. 启动数据服务（hoj-backend）

确认 MySQL、Redis、Nacos 已启动、Nacos 配置已上传后，运行：

```bash
java -jar DataBackup/target/*.jar
```

或在 IDE 中运行主类 `top.hcode.hoj.DataBackupApplication`。

### 3. 启动评测服务（hoj-judgeserver）

评测服务依赖 Go-Judge 沙盒（监听 `5050` 端口）与题目测试数据：

```bash
# 先启动安全沙盒（judger/Judger-SandBox 的可执行文件）
./Judger-SandBox

# 再启动评测服务
java -jar JudgeServer/target/*.jar
```

或在 IDE 中运行主类 `top.hcode.hoj.JudgeServerApplication`。

### 4. 联调前端

前端代码位于仓库的 `hoj-vue/` 目录，本地联调时：

```bash
cd hoj-vue
npm install
npm run serve
```

将前端请求代理到本地后端即可。前端开发的更多细节见 [自定义前端](/use/update-fe/)。

:::tip
如果只想快速体验完整功能、不打算逐模块二次开发，建议直接使用 [快速部署](/deploy/docker/) 的 docker-compose 方案，一键拉起全部服务，避免手动准备依赖与配置。
:::
