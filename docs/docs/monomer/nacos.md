# 单体部署④——Nacos部署

Nacos 在 HOJ 中承担两个角色：

1. **服务注册中心**：`hoj-backend` 与 `hoj-judgeserver` 都注册到 Nacos，后端通过它发现健康可用的评测服务实例并调度任务。
2. **配置中心**：后端的业务配置（数据库、Redis、JWT、邮件等）保存在 Nacos 中，支持动态刷新，无需重启即可改配置。

## 一、docker 部署

```shell
docker run -d \
-e JVM_XMS=384m \
-e JVM_XMX=384m \
-e JVM_XMN=192m \
-e MODE=standalone \
-e SPRING_DATASOURCE_PLATFORM=mysql \
-e MYSQL_SERVICE_HOST=mysql_host \
-e MYSQL_SERVICE_PORT=3306 \
-e MYSQL_SERVICE_USER=root \
-e MYSQL_SERVICE_PASSWORD="mysql_root_password" \
-e MYSQL_SERVICE_DB_NAME=nacos \
-e NACOS_AUTH_ENABLE=true \
-p 8848:8848 \
--name hoj-nacos \
--restart=always \
nacos/nacos-server:1.4.2
```

参数说明：

| 参数                       | 说明                                   |
| -------------------------- | -------------------------------------- |
| `MODE=standalone`          | 单机模式（生产集群可改 cluster）       |
| `SPRING_DATASOURCE_PLATFORM=mysql` | 使用 MySQL 存储 Nacos 配置      |
| `MYSQL_SERVICE_HOST/PORT`  | MySQL 地址与端口                       |
| `MYSQL_SERVICE_USER/PASSWORD` | MySQL 账号密码                     |
| `MYSQL_SERVICE_DB_NAME=nacos` | Nacos 使用的数据库名（需已初始化） |
| `NACOS_AUTH_ENABLE=true`   | 开启鉴权（需登录才能访问控制台）       |

:::warning
使用 MySQL 存储时，需要先初始化 `nacos` 库（可导入 `sqlAndsetting/nacos.sql`），否则 Nacos 无法启动。
:::

## 二、验证是否启动成功

浏览器访问 Nacos 控制台：

```text
http://服务器ip:8848/nacos
```

使用账号 `nacos`、密码 `nacos`（或你自定义的账号密码）登录，能正常进入即表示部署成功。**正式部署请务必修改默认密码。**

## 三、常规部署（非容器）

1. 下载 Nacos 1.4.2 发行包并解压：

   ```shell
   wget https://github.com/alibaba/nacos/releases/download/1.4.2/nacos-server-1.4.2.tar.gz
   tar -zxvf nacos-server-1.4.2.tar.gz
   ```

2. 使用 MySQL 存储（推荐）：编辑 `conf/application.properties`，配置数据库连接后，导入 `nacos.sql` 初始化库。

3. 单机启动：

   ```shell
   cd nacos/bin
   sh startup.sh -m standalone
   ```

4. 访问 `http://ip:8848/nacos` 验证。

:::tip
后端通过 `NACOS_URL`（默认 `127.0.0.1:8848`）连接 Nacos，部署时请确保该地址与端口对后端可达，且防火墙已放行 **8848** 端口。
:::
