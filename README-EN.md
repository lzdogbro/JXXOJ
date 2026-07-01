# Hcode Online Judge（HOJ）

![logo](./logo.png)

> An open source online judge system based on SpringBoot, SpringCloud Alibaba and Vue.js !

[![Java](https://img.shields.io/badge/Java-1.8-informational)](http://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.2.6.RELEASE-success)](https://spring.io/projects/spring-boot)
[![SpringCloud Alibaba](https://img.shields.io/badge/Spring%20Cloud%20Alibaba-2.2.1.RELEASE-success)](https://spring.io/projects/spring-cloud-alibaba)
[![MySQL](https://img.shields.io/badge/MySQL-8.0.19-blue)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-5.0.9-red)](https://redis.io/)
[![Nacos](https://img.shields.io/badge/Nacos-1.4.2-%23267DF7)](https://github.com/alibaba/nacos)
[![Vue](https://img.shields.io/badge/Vue-2.6.11-success)](https://cn.vuejs.org/)
[![Github Star](https://img.shields.io/github/stars/HimitZH/HOJ?style=social)](https://github.com/HimitZH/HOJ)
[![Gitee Star](https://gitee.com/himitzh0730/hoj/badge/star.svg)](https://gitee.com/himitzh0730/hoj)
[![QQ Group 598587305](https://img.shields.io/badge/QQ%20Group-598587305-blue)](https://qm.qq.com/cgi-bin/qm/qr?k=WWGBZ5gfDiBZOcpNvM8xnZTfUq7BT4Rs&jump_from=webapi)

[简体中文](./README.md) | English

## Overview

- One click deployment based on **Docker** and **Docker-compose**
- Multi-language support: **C, C++, C#, Python, PyPy, Go, Java, JavaScript, PHP, Ruby, Rust**
- Remote judge support: **HDU, POJ, Codeforces (including GYM), AtCoder, SPOJ, LIBRE**
- Perfect evaluation mode: **General, Special, Interactive, Self-test, Subtask, File IO**
- Perfect contest function: **Star team, Attention team, External Rank, Scroll Board**
- **Support group and discussion area**
- **⚡ PK Battle**: Real-time 1v1 programming battles with an ELO-like scoring system
- **💬 Private Chat**: One-on-one instant messaging between users

|            Online Demo             |                Documents                 |               Repositories               |         QQ Group          |
| :--------------------------------: | :--------------------------------------: | :--------------------------------------: | :-----------------------: |
| [https://hdoi.cn](https://hdoi.cn) | [https://docs.hdoi.cn](https://docs.hdoi.cn) | [GitHub](https://github.com/HimitZH/HOJ) · [Gitee](https://gitee.com/himitzh0730/hoj) | 598587305（Full）· 743568562 |

## New Features

### PK Battle (1v1 Real-time Coding Duel)

Two users compete on the same problem with a 20-minute time limit. First to get AC wins!

- **Quick Invite**: Invite another user to a PK match directly from the problem page
- **Real-time Battle UI**: Split-screen layout — problem description on the left, code editor on the right, countdown timer at the top
- **Scoring System**: Winner +10 pts, loser -2 pts, draws unchanged; scores displayed on user profiles
- **Match History**: View all PK history with win/loss details and score changes
- **Auto-timeout**: 20-minute countdown; match ends in a draw if time runs out
- **Surrender**: Players can surrender at any time to end the match

### Private Chat (User-to-User Messaging)

Direct one-on-one messaging between users, complementing the existing notification system.

- **Contact List**: Auto-populated from conversation history, showing last message and unread count
- **Real-time Messaging**: Send and receive messages with periodic refresh
- **Unread Badge**: Navigation bar shows unread private message count
- **Quick Access**: Initiate a chat directly from any user's profile page

> 📌 **Note**: Upgrading to the version with PK and Private Chat requires SQL schema changes (new `pk_match` and `private_chat` tables, plus `pk_score` column in `user_record`). See `sqlAndsetting/hoj.sql` for details.

---

## Changelog

| Date       | Content                                                  | Contributor    |
| ---------- | -------------------------------------------------------- | -------------- |
| 2020-10-26 | Development started                                      | Himit_ZH       |
| 2021-04-10 | First online test                                        | Himit_ZH       |
| 2021-04-15 | Judge scheduling 2.0 to solve concurrency issues         | Himit_ZH       |
| 2021-04-16 | Refactored JudgeServer, added deployment docs            | Himit_ZH       |
| 2021-04-19 | Added rsync for judge data sync, fixed known bugs        | Himit_ZH       |
| 2021-04-24 | Added problem templates, updated page footer             | Himit_ZH       |
| 2021-05-02 | Fixed rank list invalidation after contest rejudge       | Himit_ZH       |
| 2021-05-09 | Added public discussion, problem discussion, contest comments | Himit_ZH  |
| 2021-05-12 | Added comment/reply deletion, discussion report          | Himit_ZH       |
| 2021-05-16 | Improved permission control, discussion admin management | Himit_ZH       |
| 2021-05-22 | Updated docker-compose one-click deploy, fixed bugs      | Himit_ZH       |
| 2021-05-24 | Changed judge scheduling optimistic lock to pessimistic   | Himit_ZH       |
| 2021-05-28 | Added import/export problems, last login on user page    | Himit_ZH       |
| 2021-06-02 | Major update: improved frontend, fixed judge timeout, bug fixes | Himit_ZH |
| 2021-06-07 | Fixed special judge, added frontend i18n                 | Himit_ZH       |
| 2021-06-08 | Added admin i18n, route lazy loading                     | Himit_ZH       |
| 2021-06-12 | Improved contest modes, see online docs for details      | Himit_ZH       |
| 2021-06-14 | Improved admin permission control, restored CF vjudge    | Himit_ZH       |
| 2021-06-25 | Enhanced frontend interactions, added POJ vjudge         | Himit_ZH       |
| 2021-08-14 | Added testlib support for SPJ                            | Himit_ZH       |
| 2021-09-21 | Added contest printing, account restriction              | Himit_ZH       |
| 2021-10-05 | Added notification system: comments, replies, likes, system notices | Himit_ZH |
| 2021-10-06 | Beautified contest rank, added FPS problem import        | Himit_ZH       |
| 2021-12-09 | Added external rank, star teams, attention teams         | Himit_ZH       |
| 2022-01-01 | Added public training and training lists                 | Himit_ZH       |
| 2022-01-04 | Added interactive judge, refactored 3 judge modes        | Himit_ZH       |
| 2022-01-29 | Refactored Remote Judge, added AtCoder, SPOJ             | Himit_ZH       |
| 2022-02-19 | Redesigned homepage and problem list page                | Himit_ZH       |
| 2022-02-25 | Added PyPy2, PyPy3, JavaScript V8, JavaScript Node, PHP  | Himit_ZH       |
| 2022-03-12 | Refactored all backend APIs, added caching for contest rank | Himit_ZH    |
| 2022-03-28 | Merged group feature by 冷蕴                              | Himit_ZH, 冷蕴 |
| 2022-04-01 | Group feature officially launched                        | Himit_ZH, 冷蕴 |
| 2022-05-29 | Added online debug, submission heatmap on user profile   | Himit_ZH       |
| 2022-08-06 | Added problem tag category management (two-level tags)   | Himit_ZH       |
| 2022-08-21 | Added manual judge, cancel judge                         | Himit_ZH       |
| 2022-08-30 | Added OI subtask, ACM 'stop on error' mode               | Himit_ZH       |
| 2022-10-04 | Added contest award config, ACM scroll board             | Himit_ZH       |
| 2022-11-14 | Added focus mode for problem details, optimized homepage | Himit_ZH       |
| 2023-05-01 | Added file IO support for problems                       | Himit_ZH       |
| 2023-06-11 | Allowed submission after contest ends                    | Himit_ZH       |
| 2023-06-27 | Added Ruby, Rust support                                 | Himit_ZH       |
| 2024-03-13 | Added LibreOJ remote judge                               | Himit_ZH, Nine |
| 2025-06-25 | Added email sending for real-name verification           |                |
| 2026-06-03 | **Forked to JXXOJ**: Secondary development of HOJ        | lzdogbro       |
| 2026-06-10 | **Added Private Chat**: 1-on-1 messaging, contact list, unread badges | lzdogbro |
| 2026-06-18 | **Added PK Battle**: 1v1 real-time coding duel, 20-min limit, scoring system | lzdogbro |

---

## Installation

Please run HOJ on the following platforms:

- **Ubuntu 18.04** and above
- **CentOS Linux release 8.0** and above

And the server configuration requirements for stable operation:

- **CPU: 2 cores** and above
- **Memory: 4G** and above

For installation options and troubleshooting tips, see [HOJ Documents](https://docs.hdoi.cn/deploy/docker).

> One click deployment based on Docker & Docker-compose

If your system already has **Docker** and **Docker-compose** and you want to quickly try out HOJ, run the following commands:

```shell
sudo apt-get update && sudo apt-get install -y vim curl git

sudo apt-get update

git clone https://github.com/HimitZH/HOJ-Deploy.git && cd HOJ-Deploy && cd standAlone

# Change some configuration such as passwords.
vim .env

sudo docker-compose up -d
```

Depending on your network speed, setup completes automatically in about 5–20 minutes without manual intervention.

Wait for the command to finish, then run `docker ps`. When all containers show `healthy` status, HOJ has started successfully.

## Browser Support

Modern browsers (Chrome, Firefox) and Internet Explorer 10+. Mobile browsers are also supported.

## Screenshots

**Note: you can switch languages at the bottom of the page (Chinese & English).**

### 1. Home

![Home (English)](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/f6792ddc05f34527bdf744fa4d6d5c88.png)

### 2. Notifications

> System notification

![System notification](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/a1a83ff01be84406954537e2ab78d999.png)

> Reply to me

![Reply to me](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/513e7e37f52f48518c2fa1bf14eeea99.png)

### 3. Problem

> Problem List

![Problem List](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/d9ba009c757d48b590debe3a409c571f.png)

> Problem Details

![Problem Details](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/9f872dc1974f45c389e084f0e31a5217.png)

### 4. Training

> Training List

![Training List](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/58ac74824fcf4963810beea7ba1203b9.png)

> Training Problem List

![Training Problem List](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/b366a6a628984995b57a49c565a2ec47.png)

### 5. Contest

> Contest List

![Contest List](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/00a0438a576d43edbab676b829a38922.png)

> Contest Details

![Contest Details](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/50026bde6dd64cd5929b38f8ecc6e72e.png)

> Contest Problem List

![Contest Problem List](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/8646fc212b5c47e9b35e60634cfc8d6a.png)

> Contest Rank

- ICPC/ACM

  ![ACM Rank](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/c50140e3b73d482d82ca6f13f47aa080.png)

- OI / IOI

  ![OI Rank](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/67f6262854bb44efa70c374f1f156166.png)

- Scroll Board

  ![Scroll Board](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/8f8258babd3f43f78802144e7ecf18fe.png)

### 6. Submission

> Submission List

![Submission List](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/20210609213021223.png)

### 7. Rank

![Rank](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/407ad16361f34b44a282b07af68825e0.png)

### 8. Group

> Group List

![Group List](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/7988504326c843ef94e937a2b4f32f03.png)

> Group Details

![Group Details](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/2c05e44f5a464381b9a357aff37b0086.png)

### 9. Discussion

> Discussion List

![Discussion List](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/20210513134216723.png)

> Comment

![Comment](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/20210513142826730.png)

### 10. User Info

> User Home

![User Home](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/7d3e99dbc6fe4739a0720fcc019b2b6e.png)

> Change User Info

![Change User Info](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/971566eeac674d388b9f5d6064286e14.png)

### 11. Admin

> Admin Home Page

![Admin Home](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/9b9674c0f30a441bb200a32756f24d2c.png)

### 12. Mobile

![Mobile](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/c7b3648217af4899bedf7f7d804968ba.png)

![Mobile Comment](https://cdn.jsdelivr.net/gh/HimitZH/HOJ/docs/docs/.vuepress/public/20210509233845230.png)

### 13. Private Chat (NEW 🆕)

![Private Chat](image.png)

### 14. PK Battle (NEW 🆕)

> PK Battle page: Split-screen layout (problem + editor), with player info and countdown at top

![Jump to PK from problem page](image-2.png)
![PK Battle page](image-3.png)

> PK Match History: View all past battles with opponent info, results, and score changes

![PK History](image-1.png)

## Support & Contribution

- If you find HOJ helpful, please give the project a star ⭐
- If you found any bug, please feel free to contact me (DogBro lzzhaoning@163.com) or open an issue
- Thanks to everyone that contributes to this project

**Acknowledgments**: Thanks to [HimitZH](https://github.com/HimitZH) for the excellent original project, and all contributors for their hard work.

## License

[MIT](http://opensource.org/licenses/MIT)
