# HOJ 文档站

本目录是 HOJ 在线评测系统的官方文档站点，基于 [VuePress 1.x](https://v1.vuepress.vuejs.org/) + [vuepress-theme-hope](https://vuepress-theme-hope.github.io/v1/) 构建。

## 目录结构

```
docs/
├── README.md              # 本文件：文档站说明
├── package.json           # 文档站依赖与脚本
├── docs/                  # VuePress 内容根目录（dev/build 均以它为目标）
│   ├── .vuepress/
│   │   ├── config.js      # 站点配置、导航栏、侧边栏
│   │   └── public/        # 静态资源（图片、图标）
│   ├── README.md          # 站点首页
│   ├── introduction/      # 项目介绍与系统架构
│   ├── deploy/            # 快速部署（Docker）与运维
│   ├── monomer/           # 单体（单机）分模块部署
│   ├── develop/           # 开发文档（后端、数据库、判题调度、沙盒）
│   └── use/               # 使用文档（题目、比赛、训练、团队等）
└── 作业功能设计.md          # JXXOJ 项目自身的「作业」功能设计（不属于文档站内容）
```

## 本地运行

> 需要 Node.js 环境（建议 12.x 及以上）与 yarn 包管理器。

```bash
# 1. 安装依赖（首次）
cd docs
yarn install

# 2. 启动本地开发服务器（默认 http://localhost:8080）
yarn dev

# 3. 构建静态站点（产物输出到 docs/docs/.vuepress/dist）
yarn build
```

## 如何编写 / 修改文档

1. **新增一篇文档**：在对应的分类目录（`introduction` / `deploy` / `monomer` / `develop` / `use`）下新建 `.md` 文件，使用 Markdown 语法编写。
2. **挂到侧边栏**：编辑 `docs/docs/.vuepress/config.js` 中的 `themeConfig.sidebar`，把新文件路径加入对应分组的 `children` 数组。
3. **引用图片**：将图片放到 `docs/docs/.vuepress/public/`，在文档中用 `![说明](/图片文件名.png)` 引用（以 `/` 开头的绝对路径）。
4. **交叉引用**：站内跳转使用绝对路径，例如 `[快速部署](/deploy/docker/)`。

> 常见容器提示语：`:::tip` / `:::info` / `:::warning` / `:::danger` 包裹的内容会渲染为带图标的高亮块（vuepress-theme-hope 特性）。
