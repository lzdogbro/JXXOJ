#!/bin/bash
# =============================================================================
# HOJ (Hcode Online Judge) Deploy Script
# 用于构建项目并生成/更新 myhoj-deploy 部署目录
#
# 用法:
#   ./deploy.sh              # 构建并同步到 myhoj-deploy
#   ./deploy.sh build        # 仅构建后端 + 前端
#   ./deploy.sh init         # 初始化 myhoj-deploy 目录（首次部署）
#   ./deploy.sh sync         # 仅同步构建产物到 myhoj-deploy
#   ./deploy.sh deploy       # 完整构建 + 同步 + 重启容器
#   ./deploy.sh up           # 仅重启 docker-compose
#
# =============================================================================

set -e

# ---- 颜色输出 ----
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

log_info()  { echo -e "${GREEN}[INFO]${NC}  $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC}  $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }
log_step()  { echo -e "${CYAN}[STEP]${NC}  $1"; }

# ---- 路径配置 ----
HOJ_ROOT="$(cd "$(dirname "$0")" && pwd)"
MYHOJ_DEPLOY_DIR="${MYHOJ_DEPLOY_DIR:-${HOJ_ROOT}/../myhoj-deploy}"
BACKEND_DIR="${HOJ_ROOT}/hoj-springboot"
FRONTEND_DIR="${HOJ_ROOT}/hoj-vue"
SCROLL_BOARD_DIR="${HOJ_ROOT}/hoj-scrollBoard"
SQL_DIR="${HOJ_ROOT}/sqlAndsetting"

# ---- 可配置版本号 ----
BACKEND_JAR_NAME="${BACKEND_JAR_NAME:-hoj-backend-4.6.jar}"

# =============================================================================
# 构建函数
# =============================================================================

build_backend() {
    log_step "构建后端 JAR..."
    cd "${BACKEND_DIR}"
    mvn package -DskipTests -q
    local jar_path="${BACKEND_DIR}/DataBackup/target/${BACKEND_JAR_NAME}"
    if [ ! -f "$jar_path" ]; then
        log_error "后端 JAR 构建失败: ${jar_path} 不存在"
        exit 1
    fi
    log_info "后端构建完成: ${jar_path}"
    cd "${HOJ_ROOT}"
}

build_frontend() {
    log_step "构建前端 dist..."
    cd "${FRONTEND_DIR}"
    NODE_OPTIONS=--openssl-legacy-provider npm run build
    if [ ! -d "${FRONTEND_DIR}/dist" ]; then
        log_error "前端构建失败: dist 目录不存在"
        exit 1
    fi
    log_info "前端构建完成: ${FRONTEND_DIR}/dist"
    cd "${HOJ_ROOT}"
}

# =============================================================================
# 初始化 myhoj-deploy 目录结构
# =============================================================================

init_deploy_dir() {
    local D="${MYHOJ_DEPLOY_DIR}"

    # 如果已存在则先删除
    if [ -d "${D}" ]; then
        log_warn "myhoj-deploy 目录已存在，正在删除: ${D}"
        rm -rf "${D}"
    fi

    log_step "初始化 myhoj-deploy 目录结构..."

    # 创建目录结构
    mkdir -p "${D}/standAlone"
    mkdir -p "${D}/src/backend"
    mkdir -p "${D}/src/frontend/html"
    mkdir -p "${D}/src/frontend/scrollBoard"
    mkdir -p "${D}/src/mysql"
    mkdir -p "${D}/src/mysql-checker"
    mkdir -p "${D}/src/rsync"
    mkdir -p "${D}/src/judgeserver"
    mkdir -p "${D}/distributed/main"
    mkdir -p "${D}/distributed/judgeserver"
    mkdir -p "${D}/hoj"

    # ---- src/backend/Dockerfile ----
    cat > "${D}/src/backend/Dockerfile" << 'DOCKERFILE_EOF'
FROM adoptopenjdk/openjdk8

COPY *.jar /app.jar

COPY check_nacos.sh /check_nacos.sh

COPY run.sh /run.sh

ENV TZ=Asia/Shanghai

ENV BACKEND_SERVER_PORT=6688

VOLUME ["/hoj/file","/hoj/testcase"]

RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

CMD ["bash","/check_nacos.sh"]

EXPOSE $BACKEND_SERVER_PORT
DOCKERFILE_EOF

    # ---- src/backend/check_nacos.sh ----
    cat > "${D}/src/backend/check_nacos.sh" << 'EOF'
#!/bin/bash

while :
    do
        # 访问nacos注册中心，获取http状态码
        CODE=`curl -I -m 10 -o /dev/null -s -w %{http_code}  http://$NACOS_URL/nacos/index.html`
        # 判断状态码为200
        if [[ $CODE -eq 200 ]]; then
            # 输出绿色文字，并跳出循环
            echo -e "\033[42;34m nacos is ok \033[0m"
            break
        else
            # 暂停1秒
            sleep 1
        fi
    done

# while结束时，执行容器中的run.sh。
bash /run.sh
EOF

    # ---- src/backend/run.sh ----
    cat > "${D}/src/backend/run.sh" << 'EOF'
#!/bin/bash
if test -z "$JAVA_OPTS";then
	java -Djava.security.egd=file:/dev/./urandom -jar  /app.jar
else
	java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar  /app.jar
fi
EOF

    # ---- src/frontend/Dockerfile ----
    cat > "${D}/src/frontend/Dockerfile" << 'DOCKERFILE_EOF'
FROM nginx:1.15-alpine

COPY default.conf.template /etc/nginx/conf.d/default.conf.template

COPY default.conf.ssl.template /etc/nginx/conf.d/default.conf.ssl.template

ADD html/ /usr/share/nginx/html/

ADD scrollBoard/ /usr/share/nginx/scrollBoard/

COPY ./run.sh /docker-entrypoint.sh

RUN chmod a+x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]

# 每次容器启动时执行
CMD ["nginx", "-g", "daemon off;"]

# 容器应用端口
EXPOSE 80

EXPOSE 443
DOCKERFILE_EOF

    # ---- src/frontend/run.sh ----
    cat > "${D}/src/frontend/run.sh" << 'EOF'
#!/usr/bin/env sh
set -eu
if [ "$USE_HTTPS" == "true" ]; then
	envsubst '${SERVER_NAME} ${BACKEND_SERVER_HOST} ${BACKEND_SERVER_PORT}' < /etc/nginx/conf.d/default.conf.ssl.template > /etc/nginx/conf.d/default.conf
else
	envsubst '${SERVER_NAME} ${BACKEND_SERVER_HOST} ${BACKEND_SERVER_PORT}' < /etc/nginx/conf.d/default.conf.template > /etc/nginx/conf.d/default.conf
fi
exec "$@"
EOF
    chmod +x "${D}/src/frontend/run.sh"

    # ---- src/frontend/default.conf.template ----
    cat > "${D}/src/frontend/default.conf.template" << 'EOF'
server {
	listen 80;
	server_name ${SERVER_NAME};
	root /usr/share/nginx/html;
    location /api{
		proxy_pass http://${BACKEND_SERVER_HOST}:${BACKEND_SERVER_PORT};
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
	    proxy_set_header   Host                 $http_host;
        proxy_set_header   X-Forwarded-Proto    $scheme;

        client_max_body_size 512M;
    }
    location ~ .*\.(js|json|css)$ {
            gzip on;
            gzip_static on;
            gzip_min_length 1k;
            gzip_http_version 1.1;
            gzip_comp_level 9;
            gzip_types  text/css application/javascript application/json;
            root /usr/share/nginx/html;
    }
    location / {
            index index.html;
            try_files $uri $uri/ /index.html;
    }
	location ^~ /scrollBoard{
        alias   /usr/share/nginx/scrollBoard;
        try_files $uri $uri/ /index.html;
        index index.html index.htm;
    }

}
EOF

    # ---- src/frontend/default.conf.ssl.template ----
    cat > "${D}/src/frontend/default.conf.ssl.template" << 'EOF'
server {
    listen 80;
    server_name ${SERVER_NAME};
    return 301 https://$host$request_uri;
}

server {
	listen 443 ssl;
	server_name ${SERVER_NAME};
    ssl_certificate /etc/nginx/etc/crt/server.crt;
    ssl_certificate_key /etc/nginx/etc/crt/server.key;
    ssl_session_timeout 5m;
    ssl_protocols TLSv1 TLSv1.1 TLSv1.2;
    ssl_ciphers ECDHE-RSA-AES128-GCM-SHA256:HIGH:!aNULL:!MD5:!RC4:!DHE;
    ssl_prefer_server_ciphers on;

	root /usr/share/nginx/html;
    location /api{
		proxy_pass http://${BACKEND_SERVER_HOST}:${BACKEND_SERVER_PORT};
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
	    proxy_set_header   Host                 $http_host;
        proxy_set_header   X-Forwarded-Proto    $scheme;

        client_max_body_size 512M;
    }
    location ~ .*\.(js|json|css)$ {
            gzip on;
            gzip_static on;
            gzip_min_length 1k;
            gzip_http_version 1.1;
            gzip_comp_level 9;
            gzip_types  text/css application/javascript application/json;
            root /usr/share/nginx/html;
    }
    location / {
            index index.html;
            try_files $uri $uri/ /index.html;
    }
	location ^~ /scrollBoard{
        alias   /usr/share/nginx/scrollBoard;
        try_files $uri $uri/ /index.html;
        index index.html index.htm;
    }
}
EOF

    # ---- src/mysql/Dockerfile ----
    cat > "${D}/src/mysql/Dockerfile" << 'DOCKERFILE_EOF'
FROM mysql:8.0

ENV WORK_PATH=/usr/local/work
ENV AUTO_RUN_DIR=/docker-entrypoint-initdb.d
ENV FILE_0=hoj.sql
ENV FILE_1=nacos.sql
ENV FILE_2=nacos-data.sql
ENV INSTALL_DATA_SHELL=run.sh
ENV NACOS_DATA_SHELL=bcrypt
ENV NACOS_USERNAME=${NACOS_USERNAME}
ENV NACOS_PASSWORD=${NACOS_PASSWORD}

COPY ./$FILE_0 $WORK_PATH/
COPY ./$FILE_1 $WORK_PATH/
COPY ./$INSTALL_DATA_SHELL $AUTO_RUN_DIR/

ARG TARGETARCH
COPY ./bcrypt* $WORK_PATH/
RUN if [ "$TARGETARCH" = "arm64" ] || [ "$TARGETARCH" = "aarch64" ]; then \
        if [ -f "$WORK_PATH/bcrypt_arm" ]; then \
            mv $WORK_PATH/bcrypt_arm $WORK_PATH/bcrypt && rm -f $WORK_PATH/bcrypt_*; \
        fi \
    else \
        rm -f $WORK_PATH/bcrypt_arm; \
    fi

COPY ./mysql.cnf /etc/mysql/conf.d/

RUN chmod a+x $WORK_PATH/bcrypt

RUN echo '' > $WORK_PATH/$FILE_2

RUN chmod +777 $WORK_PATH/$FILE_2

RUN chmod a+x $AUTO_RUN_DIR/$INSTALL_DATA_SHELL
DOCKERFILE_EOF

    # ---- src/mysql/run.sh ----
    cat > "${D}/src/mysql/run.sh" << 'EOF'
#!/bin/bash

$WORK_PATH/bcrypt --username=$NACOS_USERNAME --password=$NACOS_PASSWORD --filepath=$WORK_PATH/$FILE_2;

sleep 2;

mysql -uroot -p$MYSQL_ROOT_PASSWORD << ENDSQL
system echo '================Start create database hoj====================';
source $WORK_PATH/$FILE_0;
system echo '================Start create database nacos==================';
source $WORK_PATH/$FILE_1;
system echo '================Start insert user into nacos=================';
source $WORK_PATH/$FILE_2;
system echo '=====================Everything is ok!=======================';
ENDSQL
EOF

    # ---- src/mysql/mysql.cnf ----
    cat > "${D}/src/mysql/mysql.cnf" << 'EOF'
[client]
default-character-set=utf8mb4

[mysql]
default-character-set=utf8mb4

[mysqld]
default_authentication_plugin=mysql_native_password
character-set-client-handshake=FALSE
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci

innodb_buffer_pool_size=128M
innodb_log_file_size=64M
innodb_flush_method=O_DIRECT
innodb_flush_log_at_trx_commit=2

max_connections=200
max_connect_errors=1000
EOF

    # ---- src/mysql-checker/Dockerfile ----
    cat > "${D}/src/mysql-checker/Dockerfile" << 'DOCKERFILE_EOF'
FROM arey/mysql-client

COPY ./hoj-update.sql /sql/

COPY ./update.sh /sql/

ENTRYPOINT ["/bin/sh", "/sql/update.sh"]
DOCKERFILE_EOF

    # ---- src/mysql-checker/update.sh ----
    cat > "${D}/src/mysql-checker/update.sh" << 'EOF'
#!/bin/sh

mysql -h mysql -uroot -p$MYSQL_ROOT_PASSWORD -e "select version();" &> /dev/null
RETVAL=$?

while [ $RETVAL -ne 0 ]
do
	sleep 3
	mysql -h mysql -uroot -p$MYSQL_ROOT_PASSWORD -e "select version();" &> /dev/null
	RETVAL=$?
done
mysql -uroot -h mysql -p$MYSQL_ROOT_PASSWORD -D hoj -e "source /sql/hoj-update.sql"
echo 'Check whether the `hoj` database has been updated successfully!'
EOF

    # ---- src/rsync/Dockerfile ----
    cat > "${D}/src/rsync/Dockerfile" << 'DOCKERFILE_EOF'
FROM ubuntu:18.04

RUN apt-get update && apt-get -y install rsync

RUN mkdir -p /hoj/rsyncd

COPY run.sh /hoj/rsyncd/run.sh

COPY rsyncd.conf /hoj/rsyncd/rsyncd.conf

CMD /bin/bash /hoj/rsyncd/run.sh && tail -f /dev/null
DOCKERFILE_EOF

    # ---- src/rsync/rsyncd.conf ----
    cat > "${D}/src/rsync/rsyncd.conf" << 'EOF'
port = 873
uid = root
gid = root
use chroot = yes
read only = yes
log file = /hoj/log/rsyncd.log
[testcase]
path = /hoj/testcase/
list = yes
auth users = hojrsync
secrets file = /hoj/rsyncd/rsyncd_master.passwd
EOF

    # ---- src/rsync/run.sh ----
    cat > "${D}/src/rsync/run.sh" << 'EOF'
#!/usr/bin/bash
if [ "$RSYNC_MODE" == "master" ]; then
	echo "$RSYNC_USER:$RSYNC_PASSWORD" > /hoj/rsyncd/rsyncd_master.passwd
	chmod 600 /hoj/rsyncd/rsyncd_master.passwd
	rsync --daemon --config=/hoj/rsyncd/rsyncd.conf
else
	echo "$RSYNC_PASSWORD" > /hoj/rsyncd/rsyncd_slave.passwd
	chmod 600 /hoj/rsyncd/rsyncd_slave.passwd
	while true
	do
		rsync -avz --delete --progress --password-file=/hoj/rsyncd/rsyncd_slave.passwd $RSYNC_USER@$RSYNC_MASTER_ADDR::testcase /hoj/testcase >> /hoj/log/rsync_slave.log
		sleep 100
	done
fi
EOF

    # ---- standAlone/.env ----
    cat > "${D}/standAlone/.env" << 'EOF'
# hoj全部数据存储的文件夹位置（默认当前路径生成hoj文件夹）
HOJ_DATA_DIRECTORY=../hoj

# redis的配置
REDIS_HOST=172.20.0.2
REDIS_PORT=6380
REDIS_PASSWORD=hdasjfg3432hghjk

# mysql的docker内网ip
MYSQL_HOST=172.20.0.3
# mysql暴露的端口号
MYSQL_PUBLIC_PORT=3391
# mysql的密码
MYSQL_ROOT_PASSWORD=hgj524h2jk652hk215hjk3gh
# 如果判题服务是分布式，请提供当前mysql所在服务器的公网ip
MYSQL_PUBLIC_HOST=172.20.0.3

# nacos的配置
NACOS_HOST=172.20.0.4
NACOS_PORT=8849
NACOS_USERNAME=root
NACOS_PASSWORD=hjkk436h23kgjhgkh5h45k

# backend后端服务的配置
BACKEND_HOST=172.20.0.5
BACKEND_PORT=6688
# token加密秘钥 默认则生成32位随机密钥
JWT_TOKEN_SECRET=default
# token过期时间默认为24小时 86400s
JWT_TOKEN_EXPIRE=86400
# token默认12小时可自动刷新
JWT_TOKEN_FRESH_EXPIRE=43200
# 调用判题服务器的token 默认则生成32位随机密钥
JUDGE_TOKEN=default
# 请使用邮件服务的域名或ip
EMAIL_SERVER_HOST=smtp.qq.com
EMAIL_SERVER_PORT=465
EMAIL_USERNAME=your_email_username
EMAIL_PASSWORD=your_email_password
# 开启虚拟判题请提供对应oj的账号密码 格式为
# username1,username2,...
# password1,password2,...
HDU_ACCOUNT_USERNAME_LIST=
HDU_ACCOUNT_PASSWORD_LIST=
CF_ACCOUNT_USERNAME_LIST=
CF_ACCOUNT_PASSWORD_LIST=
POJ_ACCOUNT_USERNAME_LIST=
POJ_ACCOUNT_PASSWORD_LIST=
ATCODER_ACCOUNT_USERNAME_LIST=
ATCODER_ACCOUNT_PASSWORD_LIST=
SPOJ_ACCOUNT_USERNAME_LIST=
SPOJ_ACCOUNT_PASSWORD_LIST=
LIBRE_ACCOUNT_USERNAME_LIST=
LIBRE_ACCOUNT_PASSWORD_LIST=
# 是否强制使用上面配置的账号覆盖系统原有的账号列表
FORCED_UPDATE_REMOTE_JUDGE_ACCOUNT=false

# judgeserver的配置
JUDGE_SERVER_IP=172.20.0.7
JUDGE_SERVER_PORT=8088
JUDGE_SERVER_NAME=judger-alone
# -1表示可接收最大判题任务数为cpu核心数+1
MAX_TASK_NUM=-1
# 当前判题服务器是否开启远程虚拟判题功能
REMOTE_JUDGE_OPEN=true
# -1表示可接收最大远程判题任务数为cpu核心数*2+1
REMOTE_JUDGE_MAX_TASK_NUM=-1
# 默认沙盒并行判题程序数为cpu核心数
PARALLEL_TASK=default

# docker network的配置
SUBNET=172.20.0.0/16
EOF

    # ---- standAlone/docker-compose.yml ----
    cat > "${D}/standAlone/docker-compose.yml" << 'DOCKERCOMPOSE_EOF'
version: "3"
services:
  hoj-redis:
    image: redis:5.0.9-alpine
    container_name: hoj-redis
    restart: always
    volumes:
      - ${HOJ_DATA_DIRECTORY}/data/redis/data:/data
    networks:
      hoj-network:
        ipv4_address: ${REDIS_HOST:-172.20.0.2}
    ports:
      - ${REDIS_PORT:-6379}:6379
    command: redis-server --requirepass ${REDIS_PASSWORD:-hoj123456} --appendonly yes
    healthcheck:
      test: ["CMD", "redis-cli", "-a", "${REDIS_PASSWORD:-hoj123456}", "ping"]
      interval: 10s
      timeout: 10s
      retries: 10

  hoj-mysql:
    image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_database
    container_name: hoj-mysql
    restart: always
    volumes:
      - ${HOJ_DATA_DIRECTORY}/data/mysql/data:/var/lib/mysql
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-hoj123456}
      - TZ=Asia/Shanghai
      - NACOS_USERNAME=${NACOS_USERNAME:-root}
      - NACOS_PASSWORD=${NACOS_PASSWORD:-hoj123456}
    ports:
      - ${MYSQL_PUBLIC_PORT:-3306}:3306
    networks:
      hoj-network:
        ipv4_address: ${MYSQL_HOST:-172.20.0.3}
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "root", "-p${MYSQL_ROOT_PASSWORD:-hoj123456}"]
      interval: 10s
      timeout: 10s
      retries: 10

  hoj-nacos:
    image: nacos/nacos-server:1.4.2
    container_name: hoj-nacos
    restart: always
    depends_on:
      hoj-mysql:
        condition: service_healthy
    environment:
      - JVM_XMX=384m
      - JVM_XMS=384m
      - JVM_XMN=192m
      - MODE=standalone
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=${MYSQL_HOST:-172.20.0.3}
      - MYSQL_SERVICE_PORT=3306
      - MYSQL_SERVICE_USER=root
      - MYSQL_SERVICE_PASSWORD=${MYSQL_ROOT_PASSWORD:-hoj123456}
      - MYSQL_SERVICE_DB_NAME=nacos
      - NACOS_AUTH_ENABLE=true
    ports:
      - ${NACOS_PORT:-8848}:8848
    healthcheck:
      test: curl -f http://${NACOS_HOST:-172.20.0.4}:8848/nacos/index.html || exit 1
      interval: 10s
      timeout: 10s
      retries: 12
    networks:
      hoj-network:
        ipv4_address: ${NACOS_HOST:-172.20.0.4}

  hoj-backend:
    build:
      context: ../src/backend
      dockerfile: Dockerfile
    image: myhoj/backend:v2
    container_name: hoj-backend
    restart: always
    depends_on:
      hoj-redis:
        condition: service_healthy
      hoj-mysql:
        condition: service_healthy
      hoj-nacos:
        condition: service_started
    volumes:
      - ${HOJ_DATA_DIRECTORY}/file:/hoj/file
      - ${HOJ_DATA_DIRECTORY}/testcase:/hoj/testcase
      - ${HOJ_DATA_DIRECTORY}/log/backend:/hoj/log/backend
    environment:
      - TZ=Asia/Shanghai
      - JAVA_OPTS=-Xms192m -Xmx384m
      - BACKEND_SERVER_PORT=${BACKEND_PORT:-6688}
      - NACOS_URL=${NACOS_HOST:-172.20.0.4}:8848
      - NACOS_USERNAME=${NACOS_USERNAME:-root}
      - NACOS_PASSWORD=${NACOS_PASSWORD:-hoj123456}
      - JWT_TOKEN_SECRET=${JWT_TOKEN_SECRET:-default}
      - JWT_TOKEN_EXPIRE=${JWT_TOKEN_EXPIRE:-86400}
      - JWT_TOKEN_FRESH_EXPIRE=${JWT_TOKEN_FRESH_EXPIRE:-43200}
      - JUDGE_TOKEN=${JUDGE_TOKEN:-default}
      - MYSQL_HOST=${MYSQL_HOST:-172.20.0.3}
      - MYSQL_PUBLIC_HOST=${MYSQL_PUBLIC_HOST}
      - MYSQL_PUBLIC_PORT=${MYSQL_PUBLIC_PORT:-3306}
      - MYSQL_PORT=3306
      - MYSQL_DATABASE_NAME=hoj
      - MYSQL_USERNAME=root
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-hoj123456}
      - EMAIL_SERVER_HOST=${EMAIL_SERVER_HOST:-smtp.qq.com}
      - EMAIL_SERVER_PORT=${EMAIL_SERVER_PORT:-465}
      - EMAIL_USERNAME=${EMAIL_USERNAME:-your_email_username}
      - EMAIL_PASSWORD=${EMAIL_PASSWORD:-your_email_password}
      - REDIS_HOST=${REDIS_HOST:-172.20.0.2}
      - REDIS_PORT=6379
      - REDIS_PASSWORD=${REDIS_PASSWORD:-hoj123456}
      - OPEN_REMOTE_JUDGE=true
      - HDU_ACCOUNT_USERNAME_LIST=${HDU_ACCOUNT_USERNAME_LIST}
      - HDU_ACCOUNT_PASSWORD_LIST=${HDU_ACCOUNT_PASSWORD_LIST}
      - CF_ACCOUNT_USERNAME_LIST=${CF_ACCOUNT_USERNAME_LIST}
      - CF_ACCOUNT_PASSWORD_LIST=${CF_ACCOUNT_PASSWORD_LIST}
      - POJ_ACCOUNT_USERNAME_LIST=${POJ_ACCOUNT_USERNAME_LIST}
      - POJ_ACCOUNT_PASSWORD_LIST=${POJ_ACCOUNT_PASSWORD_LIST}
      - ATCODER_ACCOUNT_USERNAME_LIST=${ATCODER_ACCOUNT_USERNAME_LIST}
      - ATCODER_ACCOUNT_PASSWORD_LIST=${ATCODER_ACCOUNT_PASSWORD_LIST}
      - SPOJ_ACCOUNT_USERNAME_LIST=${SPOJ_ACCOUNT_USERNAME_LIST}
      - SPOJ_ACCOUNT_PASSWORD_LIST=${SPOJ_ACCOUNT_PASSWORD_LIST}
      - LIBRE_ACCOUNT_USERNAME_LIST=${LIBRE_ACCOUNT_USERNAME_LIST}
      - LIBRE_ACCOUNT_PASSWORD_LIST=${LIBRE_ACCOUNT_PASSWORD_LIST}
      - FORCED_UPDATE_REMOTE_JUDGE_ACCOUNT=${FORCED_UPDATE_REMOTE_JUDGE_ACCOUNT:-false}
    ports:
      - ${BACKEND_PORT:-6688}:${BACKEND_PORT:-6688}
    networks:
      hoj-network:
        ipv4_address: ${BACKEND_HOST:-172.20.0.5}

  hoj-frontend:
    build:
      context: ../src/frontend
      dockerfile: Dockerfile
    image: myhoj/frontend:v2
    container_name: hoj-frontend
    restart: always
    environment:
      - SERVER_NAME=localhost
      - BACKEND_SERVER_HOST=${BACKEND_HOST:-172.20.0.5}
      - BACKEND_SERVER_PORT=${BACKEND_PORT:-6688}
      - USE_HTTPS=false
    ports:
      - "8003:80"
      - "4431:443"
    networks:
      hoj-network:
        ipv4_address: 172.20.0.6

  hoj-judgeserver:
    image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_judgeserver
    container_name: hoj-judgeserver
    restart: always
    depends_on:
      hoj-mysql:
        condition: service_healthy
      hoj-nacos:
        condition: service_healthy
    volumes:
      - ${HOJ_DATA_DIRECTORY}/testcase:/judge/test_case
      - ${HOJ_DATA_DIRECTORY}/judge/log:/judge/log
      - ${HOJ_DATA_DIRECTORY}/judge/run:/judge/run
      - ${HOJ_DATA_DIRECTORY}/judge/spj:/judge/spj
      - ${HOJ_DATA_DIRECTORY}/judge/interactive:/judge/interactive
      - ${HOJ_DATA_DIRECTORY}/log/judgeserver:/judge/log/judgeserver
    environment:
      - TZ=Asia/Shanghai
      - JAVA_OPTS=-Xms192m -Xmx384m
      - JUDGE_SERVER_IP=${JUDGE_SERVER_IP:-172.20.0.7}
      - JUDGE_SERVER_PORT=${JUDGE_SERVER_PORT:-8088}
      - JUDGE_SERVER_NAME=${JUDGE_SERVER_NAME:-judger-alone}
      - NACOS_URL=${NACOS_HOST:-172.20.0.4}:8848
      - NACOS_USERNAME=${NACOS_USERNAME:-root}
      - NACOS_PASSWORD=${NACOS_PASSWORD:-hoj123456}
      - MAX_TASK_NUM=${MAX_TASK_NUM:--1}
      - REMOTE_JUDGE_OPEN=${REMOTE_JUDGE_OPEN:-true}
      - REMOTE_JUDGE_MAX_TASK_NUM=${REMOTE_JUDGE_MAX_TASK_NUM:--1}
      - PARALLEL_TASK=${PARALLEL_TASK:-default}
    ports:
      - ${JUDGE_SERVER_PORT:-8088}:${JUDGE_SERVER_PORT:-8088}
    healthcheck:
      test: curl -f http://${JUDGE_SERVER_IP:-172.20.0.7}:${JUDGE_SERVER_PORT:-8088}/version || exit 1
      interval: 30s
      timeout: 10s
      retries: 3
    privileged: true
    shm_size: 512mb
    networks:
      hoj-network:
        ipv4_address: 172.20.0.7

  hoj-mysql-checker:
    image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_database_checker
    container_name: hoj-mysql-checker
    depends_on:
      hoj-mysql:
        condition: service_healthy
    links:
      - hoj-mysql:mysql
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-hoj123456}
    networks:
      hoj-network:
        ipv4_address: 172.20.0.8

  hoj-autohealth:
    restart: always
    container_name: hoj-autohealth
    image: willfarrell/autoheal
    environment:
      - AUTOHEAL_CONTAINER_LABEL=all
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock

networks:
  hoj-network:
    driver: bridge
    ipam:
      config:
        - subnet: ${SUBNET:-172.20.0.0/16}
DOCKERCOMPOSE_EOF

    # ---- distributed/main/docker-compose.yml ----
    cat > "${D}/distributed/main/docker-compose.yml" << 'DOCKERCOMPOSE_EOF'
version: "3"
services:

  hoj-redis:
    image: redis:5.0.9-alpine
    container_name: hoj-redis
    restart: always
    volumes:
      - ${HOJ_DATA_DIRECTORY}/data/redis/data:/data
    networks:
      hoj-network:
        ipv4_address: ${REDIS_HOST:-172.20.0.2}
    ports:
      - ${REDIS_PORT:-6379}:6379
    command: redis-server --requirepass ${REDIS_PASSWORD:-hoj123456} --appendonly yes

  hoj-mysql:
    image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_database
    container_name: hoj-mysql
    restart: always
    volumes:
      - ${HOJ_DATA_DIRECTORY}/data/mysql/data:/var/lib/mysql
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-hoj123456}
      - TZ=Asia/Shanghai
      - NACOS_USERNAME=${NACOS_USERNAME:-root}
      - NACOS_PASSWORD=${NACOS_PASSWORD:-hoj123456}
    ports:
      - ${MYSQL_PUBLIC_PORT:-3306}:3306
    networks:
      hoj-network:
        ipv4_address: ${MYSQL_HOST:-172.20.0.3}

  hoj-nacos:
    image: nacos/nacos-server:1.4.2
    container_name: hoj-nacos
    restart: always
    depends_on:
      - hoj-mysql
    environment:
      - JVM_XMX=384m
      - JVM_XMS=384m
      - JVM_XMN=192m
      - MODE=standalone
      - SPRING_DATASOURCE_PLATFORM=mysql
      - MYSQL_SERVICE_HOST=${MYSQL_HOST:-172.20.0.3}
      - MYSQL_SERVICE_PORT=3306
      - MYSQL_SERVICE_USER=root
      - MYSQL_SERVICE_PASSWORD=${MYSQL_ROOT_PASSWORD:-hoj123456}
      - MYSQL_SERVICE_DB_NAME=nacos
      - NACOS_AUTH_ENABLE=true
    ports:
      - ${NACOS_PORT:-8848}:8848
    healthcheck:
      test: curl -f http://${NACOS_HOST:-172.20.0.4}:8848/nacos/index.html || exit 1
      interval: 6s
      timeout: 10s
      retries: 10
    networks:
      hoj-network:
        ipv4_address: ${NACOS_HOST:-172.20.0.4}

  hoj-backend:
    build:
      context: ../../src/backend
      dockerfile: Dockerfile
    image: myhoj/backend:v2
    container_name: hoj-backend
    restart: always
    depends_on:
      - hoj-redis
      - hoj-mysql
      - hoj-nacos
    volumes:
      - ${HOJ_DATA_DIRECTORY}/file:/hoj/file
      - ${HOJ_DATA_DIRECTORY}/testcase:/hoj/testcase
      - ${HOJ_DATA_DIRECTORY}/log/backend:/hoj/log/backend
    environment:
      - TZ=Asia/Shanghai
      - BACKEND_SERVER_PORT=${BACKEND_PORT:-6688}
      - NACOS_URL=${NACOS_HOST:-172.20.0.4}:8848
      - NACOS_USERNAME=${NACOS_USERNAME:-root}
      - NACOS_PASSWORD=${NACOS_PASSWORD:-hoj123456}
      - JWT_TOKEN_SECRET=${JWT_TOKEN_SECRET:-default}
      - JWT_TOKEN_EXPIRE=${JWT_TOKEN_EXPIRE:-86400}
      - JWT_TOKEN_FRESH_EXPIRE=${JWT_TOKEN_FRESH_EXPIRE:-43200}
      - JUDGE_TOKEN=${JUDGE_TOKEN:-default}
      - MYSQL_HOST=${MYSQL_HOST:-172.20.0.3}
      - MYSQL_PUBLIC_HOST=${MYSQL_PUBLIC_HOST:-172.20.0.3}
      - MYSQL_PUBLIC_PORT=${MYSQL_PUBLIC_PORT:-3306}
      - MYSQL_PORT=3306
      - MYSQL_DATABASE_NAME=hoj
      - MYSQL_USERNAME=root
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-hoj123456}
      - EMAIL_SERVER_HOST=${EMAIL_SERVER_HOST:-smtp.qq.com}
      - EMAIL_SERVER_PORT=${EMAIL_SERVER_PORT:-465}
      - EMAIL_USERNAME=${EMAIL_USERNAME:-your_email_username}
      - EMAIL_PASSWORD=${EMAIL_PASSWORD:-your_email_password}
      - REDIS_HOST=${REDIS_HOST:-172.20.0.2}
      - REDIS_PORT=6379
      - REDIS_PASSWORD=${REDIS_PASSWORD:-hoj123456}
      - OPEN_REMOTE_JUDGE=true
      - HDU_ACCOUNT_USERNAME_LIST=${HDU_ACCOUNT_USERNAME_LIST}
      - HDU_ACCOUNT_PASSWORD_LIST=${HDU_ACCOUNT_PASSWORD_LIST}
      - CF_ACCOUNT_USERNAME_LIST=${CF_ACCOUNT_USERNAME_LIST}
      - CF_ACCOUNT_PASSWORD_LIST=${CF_ACCOUNT_PASSWORD_LIST}
      - POJ_ACCOUNT_USERNAME_LIST=${POJ_ACCOUNT_USERNAME_LIST}
      - POJ_ACCOUNT_PASSWORD_LIST=${POJ_ACCOUNT_PASSWORD_LIST}
      - ATCODER_ACCOUNT_USERNAME_LIST=${ATCODER_ACCOUNT_USERNAME_LIST}
      - ATCODER_ACCOUNT_PASSWORD_LIST=${ATCODER_ACCOUNT_PASSWORD_LIST}
      - SPOJ_ACCOUNT_USERNAME_LIST=${SPOJ_ACCOUNT_USERNAME_LIST}
      - SPOJ_ACCOUNT_PASSWORD_LIST=${SPOJ_ACCOUNT_PASSWORD_LIST}
      - LIBRE_ACCOUNT_USERNAME_LIST=${LIBRE_ACCOUNT_USERNAME_LIST}
      - LIBRE_ACCOUNT_PASSWORD_LIST=${LIBRE_ACCOUNT_PASSWORD_LIST}
      - FORCED_UPDATE_REMOTE_JUDGE_ACCOUNT=${FORCED_UPDATE_REMOTE_JUDGE_ACCOUNT:-false}
    ports:
      - ${BACKEND_PORT:-6688}:${BACKEND_PORT:-6688}
    networks:
      hoj-network:
        ipv4_address: ${BACKEND_HOST:-172.20.0.5}

  hoj-frontend:
    build:
      context: ../../src/frontend
      dockerfile: Dockerfile
    image: myhoj/frontend:v2
    container_name: hoj-frontend
    restart: always
    environment:
      - SERVER_NAME=localhost
      - BACKEND_SERVER_HOST=${BACKEND_HOST:-172.20.0.5}
      - BACKEND_SERVER_PORT=${BACKEND_PORT:-6688}
      - USE_HTTPS=false
    ports:
      - "80:80"
      - "443:443"
    networks:
      hoj-network:
        ipv4_address: 172.20.0.6

  hoj-rsync-master:
    image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_rsync:1.0
    container_name: hoj-rsync-master
    volumes:
      - ${HOJ_DATA_DIRECTORY}/testcase:/hoj/testcase:ro
    environment:
      - RSYNC_MODE=master
      - RSYNC_USER=hojrsync
      - RSYNC_PASSWORD=${RSYNC_PASSWORD:-hoj123456}
    ports:
      - "0.0.0.0:873:873"

  hoj-mysql-checker:
    image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_database_checker
    container_name: hoj-mysql-checker
    depends_on:
      - hoj-mysql
    links:
      - hoj-mysql:mysql
    environment:
      - MYSQL_ROOT_PASSWORD=${MYSQL_ROOT_PASSWORD:-hoj123456}
    networks:
      hoj-network:
        ipv4_address: 172.20.0.7

  hoj-autohealth:
    restart: always
    container_name: hoj-autohealth
    image: willfarrell/autoheal
    environment:
      - AUTOHEAL_CONTAINER_LABEL=all
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
networks:
   hoj-network:
     driver: bridge
     ipam:
       config:
         - subnet: ${SUBNET:-172.20.0.0/16}
DOCKERCOMPOSE_EOF

    # ---- distributed/main/.env ----
    cat > "${D}/distributed/main/.env" << 'EOF'
HOJ_DATA_DIRECTORY=../../hoj
REDIS_HOST=172.20.0.2
REDIS_PORT=6379
REDIS_PASSWORD=hoj123456
MYSQL_HOST=172.20.0.3
MYSQL_PUBLIC_HOST=172.20.0.3
MYSQL_PUBLIC_PORT=3306
MYSQL_ROOT_PASSWORD=hoj123456
NACOS_HOST=172.20.0.4
NACOS_PORT=8848
NACOS_USERNAME=root
NACOS_PASSWORD=hoj123456
BACKEND_HOST=172.20.0.5
BACKEND_PORT=6688
JWT_TOKEN_SECRET=default
JWT_TOKEN_EXPIRE=86400
JWT_TOKEN_FRESH_EXPIRE=43200
JUDGE_TOKEN=default
EMAIL_SERVER_HOST=smtp.qq.com
EMAIL_SERVER_PORT=465
EMAIL_USERNAME=your_email_username
EMAIL_PASSWORD=your_email_password
HDU_ACCOUNT_USERNAME_LIST=username1,username2
HDU_ACCOUNT_PASSWORD_LIST=password1,password2
CF_ACCOUNT_USERNAME_LIST=
CF_ACCOUNT_PASSWORD_LIST=
POJ_ACCOUNT_USERNAME_LIST=
POJ_ACCOUNT_PASSWORD_LIST=
ATCODER_ACCOUNT_USERNAME_LIST=
ATCODER_ACCOUNT_PASSWORD_LIST=
SPOJ_ACCOUNT_USERNAME_LIST=
SPOJ_ACCOUNT_PASSWORD_LIST=
LIBRE_ACCOUNT_USERNAME_LIST=
LIBRE_ACCOUNT_PASSWORD_LIST=
FORCED_UPDATE_REMOTE_JUDGE_ACCOUNT=false
RSYNC_PASSWORD=hoj123456
SUBNET=172.20.0.0/16
EOF

    # ---- distributed/judgeserver/docker-compose.yml ----
    cat > "${D}/distributed/judgeserver/docker-compose.yml" << 'DOCKERCOMPOSE_EOF'
version: "3"
services:

  hoj-judgeserver:
    image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_judgeserver
    container_name: hoj-judgeserver
    restart: always
    volumes:
      - ${HOJ_JUDGESERVER_DATA_DIRECTORY}/test_case:/judge/test_case
      - ${HOJ_JUDGESERVER_DATA_DIRECTORY}/log:/judge/log
      - ${HOJ_JUDGESERVER_DATA_DIRECTORY}/run:/judge/run
      - ${HOJ_JUDGESERVER_DATA_DIRECTORY}/spj:/judge/spj
      - ${HOJ_JUDGESERVER_DATA_DIRECTORY}/interactive:/judge/interactive
      - ${HOJ_JUDGESERVER_DATA_DIRECTORY}/log/judgeserver:/judge/log/judgeserver
    environment:
      - TZ=Asia/Shanghai
      - JUDGE_SERVER_IP=${JUDGE_SERVER_IP}
      - JUDGE_SERVER_PORT=${JUDGE_SERVER_PORT:-8088}
      - JUDGE_SERVER_NAME=${JUDGE_SERVER_NAME}
      - NACOS_URL=${NACOS_HOST}:${NACOS_PORT}
      - NACOS_USERNAME=${NACOS_USERNAME:-root}
      - NACOS_PASSWORD=${NACOS_PASSWORD:-hoj123456}
      - MAX_TASK_NUM=${MAX_TASK_NUM:--1}
      - REMOTE_JUDGE_OPEN=${REMOTE_JUDGE_OPEN:-true}
      - REMOTE_JUDGE_MAX_TASK_NUM=${REMOTE_JUDGE_MAX_TASK_NUM:--1}
      - PARALLEL_TASK=${PARALLEL_TASK:-default}
    ports:
      - ${JUDGE_SERVER_PORT:-8088}:${JUDGE_SERVER_PORT:-8088}
    healthcheck:
      test: curl -f http://localhost:${JUDGE_SERVER_PORT:-8088}/version || exit 1
      interval: 30s
      timeout: 10s
      retries: 3
    shm_size: 512mb
    privileged: true

  hoj-rsync-slave:
    image: registry.cn-shenzhen.aliyuncs.com/hcode/hoj_rsync:1.0
    container_name: hoj-rsync-slave
    restart: always
    volumes:
      - ${HOJ_JUDGESERVER_DATA_DIRECTORY}/test_case:/hoj/testcase
      - ${HOJ_JUDGESERVER_DATA_DIRECTORY}/log:/hoj/log
    environment:
      - RSYNC_MODE=slave
      - RSYNC_USER=hojrsync
      - RSYNC_PASSWORD=${RSYNC_PASSWORD}
      - RSYNC_MASTER_ADDR=${RSYNC_MASTER_ADDR}
    ports:
      - "0.0.0.0:873:873"
DOCKERCOMPOSE_EOF

    # ---- distributed/judgeserver/.env ----
    cat > "${D}/distributed/judgeserver/.env" << 'EOF'
HOJ_JUDGESERVER_DATA_DIRECTORY=./judge
NACOS_HOST=NACOS_HOST
NACOS_PORT=8848
NACOS_USERNAME=root
NACOS_PASSWORD=hoj123456
JUDGE_SERVER_IP=172.20.0.7
JUDGE_SERVER_PORT=8088
JUDGE_SERVER_NAME=judger-1
MAX_TASK_NUM=-1
REMOTE_JUDGE_OPEN=true
REMOTE_JUDGE_MAX_TASK_NUM=-1
PARALLEL_TASK=default
RSYNC_MASTER_ADDR=127.0.0.1
RSYNC_PASSWORD=hoj123456
EOF

    # 复制 nacos.sql 和 bcrypt 文件（如已存在）
    if [ -f "${SQL_DIR}/nacos.sql" ]; then
        cp "${SQL_DIR}/nacos.sql" "${D}/src/mysql/nacos.sql"
        log_info "已复制 nacos.sql"
    else
        log_warn "nacos.sql 不存在于 ${SQL_DIR}，请手动复制"
    fi

    if [ -f "${MYHOJ_DEPLOY_DIR}/src/mysql/bcrypt" ]; then
        log_info "bcrypt 二进制文件已存在"
    else
        log_warn "bcrypt 文件不存在，mysql 本地构建可能失败（如使用远程镜像则忽略）"
    fi

    log_info "myhoj-deploy 目录结构初始化完成: ${D}"
    echo ""
    echo "  myhoj-deploy/"
    echo "  ├── standAlone/"
    echo "  │   ├── docker-compose.yml"
    echo "  │   └── .env"
    echo "  ├── distributed/"
    echo "  │   ├── main/"
    echo "  │   └── judgeserver/"
    echo "  ├── src/"
    echo "  │   ├── backend/"
    echo "  │   ├── frontend/"
    echo "  │   ├── mysql/"
    echo "  │   ├── mysql-checker/"
    echo "  │   └── rsync/"
    echo "  └── hoj/    (数据持久化目录)"
    echo ""
}

# =============================================================================
# 同步构建产物
# =============================================================================

sync_artifacts() {
    log_step "同步构建产物到 myhoj-deploy..."

    local D="${MYHOJ_DEPLOY_DIR}"

    # 检查 myhoj-deploy 目录是否存在
    if [ ! -d "${D}" ]; then
        log_error "myhoj-deploy 目录不存在: ${D}"
        log_info "请先运行 './deploy.sh init' 初始化目录结构"
        exit 1
    fi

    # 1. 复制后端 JAR
    local jar_src="${BACKEND_DIR}/DataBackup/target/${BACKEND_JAR_NAME}"
    if [ -f "${jar_src}" ]; then
        cp "${jar_src}" "${D}/src/backend/"
        log_info "已复制后端 JAR: ${BACKEND_JAR_NAME} → src/backend/"
    else
        log_error "后端 JAR 不存在: ${jar_src}"
        log_info "请先运行 './deploy.sh build' 构建项目"
        exit 1
    fi

    # 2. 复制前端 dist
    local dist_src="${FRONTEND_DIR}/dist"
    if [ -d "${dist_src}" ]; then
        rm -rf "${D}/src/frontend/html/"*
        cp -r "${dist_src}"/* "${D}/src/frontend/html/"
        log_info "已复制前端 dist → src/frontend/html/"
    else
        log_warn "前端 dist 不存在: ${dist_src}，跳过"
    fi

    # 3. 复制滚榜页面
    if [ -d "${SCROLL_BOARD_DIR}" ]; then
        rm -rf "${D}/src/frontend/scrollBoard/"*
        cp -r "${SCROLL_BOARD_DIR}"/* "${D}/src/frontend/scrollBoard/"
        log_info "已复制滚榜页面 → src/frontend/scrollBoard/"
    else
        log_warn "滚榜目录不存在，跳过"
    fi

    # 4. 复制 SQL 文件
    if [ -f "${SQL_DIR}/hoj.sql" ]; then
        cp "${SQL_DIR}/hoj.sql" "${D}/src/mysql/"
        log_info "已复制 hoj.sql → src/mysql/"
    else
        log_warn "hoj.sql 不存在，跳过"
    fi

    log_info "构建产物同步完成!"
}

# =============================================================================
# Docker 部署控制
# =============================================================================

docker_up() {
    log_step "启动 Docker 容器..."
    cd "${MYHOJ_DEPLOY_DIR}/standAlone"

    # 停止现有容器
    log_info "停止现有容器..."
    docker-compose down 2>/dev/null || true

    # 重新构建并启动
    log_info "构建镜像并启动容器..."
    docker-compose up -d --build

    log_info "Docker 容器启动完成!"
    echo ""
    echo "  服务端口:"
    echo "    前端:    http://localhost:8003"
    echo "    后端:    http://localhost:6688"
    echo "    MySQL:   localhost:3391"
    echo "    Redis:   localhost:6380"
    echo "    Nacos:   http://localhost:8849/nacos"
    echo ""
}

docker_down() {
    log_step "停止 Docker 容器..."
    cd "${MYHOJ_DEPLOY_DIR}/standAlone"
    docker-compose down
    log_info "所有容器已停止"
}

# =============================================================================
# 主流程
# =============================================================================

show_usage() {
    echo "用法: $0 [命令]"
    echo ""
    echo "命令:"
    echo "  (无参数)    等同于 deploy —— 完整构建 + 同步 + 重启容器"
    echo "  build       仅构建后端 JAR 和前端 dist"
    echo "  init        初始化 myhoj-deploy 目录结构（首次部署时使用）"
    echo "  sync        仅同步构建产物到 myhoj-deploy（需先 build）"
    echo "  up          仅启动 Docker 容器"
    echo "  down        仅停止 Docker 容器"
    echo "  deploy      完整流程: build → init(如需) → sync → up"
    echo "  restart     重启容器（down + up，含重新构建）"
    echo "  status      查看容器运行状态"
    echo ""
    echo "环境变量:"
    echo "  MYHOJ_DEPLOY_DIR    myhoj-deploy 目录路径（默认: ../myhoj-deploy）"
    echo "  BACKEND_JAR_NAME    后端 JAR 文件名（默认: hoj-backend-4.6.jar）"
    echo ""
}

show_status() {
    cd "${MYHOJ_DEPLOY_DIR}/standAlone" 2>/dev/null || return
    echo "========== Docker 容器状态 =========="
    docker-compose ps 2>/dev/null || echo "无法获取容器状态"
    echo ""
    echo "========== 端口监听 =========="
    ss -tlnp 2>/dev/null | grep -E '6688|8003|3391|6380|8849' || echo "无相关端口监听"
}

# ---- 命令分发 ----
COMMAND="${1:-deploy}"

case "$COMMAND" in
    build)
        build_backend
        build_frontend
        log_info "构建完成! 接下来可运行 './deploy.sh sync' 同步到 myhoj-deploy"
        ;;
    init)
        init_deploy_dir
        ;;
    sync)
        sync_artifacts
        ;;
    up)
        docker_up
        ;;
    down)
        docker_down
        ;;
    restart)
        docker_down
        docker_up
        ;;
    status)
        show_status
        ;;
    deploy)
        echo "========================================"
        echo "  HOJ 一键部署"
        echo "  目标: ${MYHOJ_DEPLOY_DIR}"
        echo "========================================"
        echo ""

        # 1. 构建
        build_backend
        build_frontend

        # 2. 初始化目录（如不存在）
        if [ ! -d "${MYHOJ_DEPLOY_DIR}/standAlone" ]; then
            log_warn "myhoj-deploy 目录未初始化，正在创建..."
            init_deploy_dir
        fi

        # 3. 同步产物
        sync_artifacts

        # 4. 启动容器
        docker_up

        echo ""
        echo "========================================"
        echo "  🎉 部署完成! 访问 http://localhost:8003"
        echo "========================================"
        ;;
    *)
        echo "未知命令: $COMMAND"
        show_usage
        exit 1
        ;;
esac
