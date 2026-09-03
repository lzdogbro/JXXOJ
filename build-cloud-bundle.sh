#!/usr/bin/env bash
# =============================================================================
# build-cloud-bundle.sh —— 生成自包含的云端部署包（myhoj-deploy-cloud）
#
# 把 JXXOJ 构建产物 + 部署配置 + 数据目录打包成一个可独立部署的目录，
# 云端只需解包 + `docker compose up -d --build` 一条命令即可完成部署，
# 不需要 JXXOJ 源码、不需要 deploy.sh、不需要手动 docker cp / 数据库迁移。
#
# 用法:
#   ./build-cloud-bundle.sh                # 完整：init + sync + 复制数据 + 打包
#   ./build-cloud-bundle.sh --no-tar       # 只生成部署包目录，不打包 tarball
#   ./build-cloud-bundle.sh --skip-data    # 跳过数据目录复制（云端自行恢复数据）
#
# 环境变量（可选覆盖）:
#   BUNDLE_DIR   部署包目录（默认 ../myhoj-deploy-cloud）
#   DATA_SRC     数据来源 hoj 目录（默认 ../myhoj-deploy/hoj）
#   TARBALL      打包产物路径（默认 ../myhoj-deploy-cloud.tar.gz）
# =============================================================================
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DEPLOY_SH="${SCRIPT_DIR}/deploy.sh"

BUNDLE_DIR="${BUNDLE_DIR:-${SCRIPT_DIR}/../myhoj-deploy-cloud}"
DATA_SRC="${DATA_SRC:-${SCRIPT_DIR}/../myhoj-deploy/hoj}"
TARBALL="${TARBALL:-${SCRIPT_DIR}/../myhoj-deploy-cloud.tar.gz}"

DO_TAR=1
DO_DATA=1
for arg in "$@"; do
    case "$arg" in
        --no-tar)    DO_TAR=0 ;;
        --skip-data) DO_DATA=0 ;;
        *) echo "未知参数: $arg"; exit 1 ;;
    esac
done

log()  { echo -e "\033[32m[INFO]\033[0m  $1"; }
warn() { echo -e "\033[33m[WARN]\033[0m  $1"; }
err()  { echo -e "\033[31m[ERROR]\033[0m $1"; }

# 1. 前置检查
[ -f "$DEPLOY_SH" ] || { err "找不到 deploy.sh: $DEPLOY_SH"; exit 1; }

# 安全检查：避免误删现有的 myhoj-deploy 目录（那是本地开发环境，含还原好的数据）
if [ "$(readlink -f "$BUNDLE_DIR")" = "$(readlink -f "${SCRIPT_DIR}/../myhoj-deploy")" ]; then
    err "BUNDLE_DIR 不能指向现有的 myhoj-deploy 目录（init 会删除它）"
    err "请用默认的 myhoj-deploy-cloud，或通过 BUNDLE_DIR 指定其他新目录"
    exit 1
fi

log "部署包目录: ${BUNDLE_DIR}"
log "数据来源:   ${DATA_SRC}"

# 2. init（deploy.sh 会生成完整目录结构，含 judgeserver/mysql-checker 的 Dockerfile 与 compose build 段）
log "初始化部署包目录..."
MYHOJ_DEPLOY_DIR="$BUNDLE_DIR" "$DEPLOY_SH" init

# 3. sync（复制后端/判题 jar、前端静态、迁移脚本）
log "同步构建产物..."
MYHOJ_DEPLOY_DIR="$BUNDLE_DIR" "$DEPLOY_SH" sync

# 4. 复制数据目录（testcase / file / judge / data/mysql）
if [ "$DO_DATA" -eq 1 ]; then
    if [ -d "$DATA_SRC" ]; then
        log "复制数据目录: ${DATA_SRC} → ${BUNDLE_DIR}/hoj/"
        # 用 sudo 保留属主/权限（mysql 数据目录对 owner 敏感）
        if sudo -n cp -a "$DATA_SRC/." "$BUNDLE_DIR/hoj/" 2>/dev/null; then
            log "数据复制完成"
        else
            warn "需要 sudo 密码来复制数据（保留属主/权限），请在交互终端运行本脚本"
            sudo cp -a "$DATA_SRC/." "$BUNDLE_DIR/hoj/"
            log "数据复制完成"
        fi
    else
        warn "数据来源不存在，跳过: ${DATA_SRC}"
        warn "云端需自行恢复数据（三个 tarball + SQL 备份）"
    fi
fi

# 5. 打包 tarball
if [ "$DO_TAR" -eq 1 ]; then
    log "打包: ${TARBALL}"
    tar czf "$TARBALL" -C "$(dirname "$BUNDLE_DIR")" "$(basename "$BUNDLE_DIR")"
    log "打包完成: ${TARBALL} ($(du -h "$TARBALL" | cut -f1))"
fi

cat <<EOF

==========================================================
  部署包已生成: ${BUNDLE_DIR}
==========================================================
  请检查并修改: ${BUNDLE_DIR}/standAlone/.env
    - JWT_TOKEN_SECRET / JUDGE_TOKEN      （改为随机密钥）
    - MYSQL_ROOT_PASSWORD / REDIS_PASSWORD / NACOS_PASSWORD（改为真实密码）
    - EMAIL_USERNAME / EMAIL_PASSWORD     （如需邮件）

  云端部署步骤:
    1. 上传部署包到云端（${DO_TAR:+tarball: ${TARBALL}}）
    2. 解包（若打包了 tarball）: tar xzf myhoj-deploy-cloud.tar.gz
    3. cd myhoj-deploy-cloud/standAlone
    4. docker compose up -d --build
       —— 自动 build backend/frontend/judgeserver/mysql-checker，
          并在 MySQL 就绪后应用表结构迁移（只改表结构、不碰数据）

==========================================================
EOF
