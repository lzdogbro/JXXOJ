const { config } = require("vuepress-theme-hope");

module.exports = context => config({
    title: 'HOJ',
    head: [
        ['link', { rel: 'icon', href: `/logo.png` }],
        ['meta', { name: 'theme-color', content: '#ffeded' }],
    ],
    plugins: [
        '@vuepress/back-to-top',
        '@vuepress/active-header-links',
        'vuepress-plugin-nprogress',
        'vuepress-plugin-smooth-scroll',
        'vuepress-plugin-zooming'
    ],
    base: '/',
    themeConfig: {
        logo: '/favicon.ico',
        nav: [
            { text: 'Demo', link: 'https://hdoi.cn/' },
            { text: 'Gitee', link: 'https://gitee.com/himitzh0730/hoj' },
        ],
        pwa: {
            cacheHTML: false,
        },
        sidebarDepth: 2,
        hostname: 'https://hcode.top',
        repo: 'HimitZH/HOJ',
        pageInfo: false,
        copyright: false,
        mdEnhance: {
            align: true,
            sup: true,
            sub: true,
            footnote: true,
            tex: true,
        },
        docsRepo: 'HimitZH/HOJ',
        docsBranch: 'master',
        docsDir: 'docs/docs',
        editLinks: true,
        displayAllHeaders: true,
        smoothScroll: true,
        sidebar: [
            {
                title: '开始介绍',
                collapsable: true,
                children: [
                    'introduction/',
                    'introduction/architecture'
                ]
            },
            {
                title: '快速部署',
                collapsable: true,
                children: [
                    'deploy/env',
                    'deploy/docker',
                    'deploy/setting',
                    'deploy/open-https',
                    'deploy/multi-judgeserver',
                    'deploy/update',
                    'deploy/how-to-backup',
                    'deploy/faq'
                ]
            },
            {
                title: '单体部署',
                collapsable: true,
                children: [
                    'monomer/mysql',
                    'monomer/mysql-checker',
                    'monomer/redis',
                    'monomer/nacos',
                    'monomer/backend',
                    'monomer/judgeserver',
                    'monomer/frontend',
                    'monomer/rsync'
                ]
            },
            {
                title: '开发文档',
                collapsable: true,
                children: [
                    'develop/backend',
                    'develop/db',
                    'develop/judge_dispatcher',
                    'develop/sandbox'
                ]
            },
            {
                title: '使用文档',
                collapsable: true,
                children: [
                    // 题目与评测
                    'use/import-problem',
                    'use/testcase',
                    'use/judge-mode',
                    'use/judge-case-mode',
                    'use/cancel-judge',
                    'use/custom-difficulty',
                    // 比赛
                    'use/contest',
                    'use/scroll-board',
                    // 训练与团队
                    'use/training',
                    'use/group',
                    // 讨论与通知
                    'use/discussion-admin',
                    'use/notice-announcement',
                    // 用户管理
                    'use/import-user',
                    'use/admin-user',
                    // 前端自定义
                    'use/update-fe',
                    'use/close-free-cdn'
                ]
            },
        ],
    },
    evergreen: !!context.isProd,
})
