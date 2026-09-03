# 单体部署③——Redis部署

Redis 在 HOJ 中有两个关键作用：

1. **数据缓存**：缓存用户信息、题目数据等热点数据，降低 MySQL 压力。
2. **评测队列**：后端把待评测任务写入 Redis 的 list 结构，供评测服务消费，实现提交与评测的解耦。

## 一、docker 部署

```shell
docker run -d --name hoj-redis -p 6379:6379 \
-v $PWD/hoj/data/redis/data:/data \
--restart="always" \
redis:5.0.9-alpine \
--requirepass "hoj123456" --appendonly yes
```

- `--requirepass "hoj123456"`：设置访问密码，**正式部署请修改**。
- `--appendonly yes`：开启 AOF 持久化，避免宕机丢失数据。
- `-v $PWD/hoj/data/redis/data:/data`：把持久化数据挂载到宿主机，方便备份。

docker-compose 方式如下：

```yaml
version: "3"
services:
  hoj-redis:
    image: redis:5.0.9-alpine
    container_name: hoj-redis
    restart: always
    volumes:
      - ./hoj/data/redis/data:/data
    ports:
      - "6379:6379"
    command: redis-server --requirepass "hoj123456" --appendonly yes
```

## 二、验证是否启动成功

使用 `redis-cli` 连接并验证：

```shell
# 进入容器
docker exec -it hoj-redis redis-cli -a hoj123456 ping
# 返回 PONG 即表示启动成功
```

## 三、常规部署（非容器）

如果不想用容器，可在 Ubuntu 上直接安装：

```shell
sudo apt-get update
sudo apt-get install -y redis-server
```

安装后编辑 `/etc/redis/redis.conf`，至少修改以下两项：

```ini
# 设置访问密码（与后端配置 REDIS_PASSWORD 保持一致）
requirepass hoj123456

# 开启 AOF 持久化
appendonly yes
```

保存后重启并设为开机自启：

```shell
sudo systemctl restart redis-server
sudo systemctl enable redis-server
```

验证：

```shell
redis-cli -a hoj123456 ping
```

:::warning
无论采用哪种方式，Redis 的密码都必须与后端配置中的 `REDIS_PASSWORD` 一致，否则后端无法连接，提交评测会直接失败。
:::
