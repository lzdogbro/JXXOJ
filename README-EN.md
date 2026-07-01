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

## Support & Contribution

- If you find HOJ helpful, please give the project a star ⭐
- If you found any bug, please feel free to contact us via [QQ Group: 598587305](https://qm.qq.com/cgi-bin/qm/qr?k=WWGBZ5gfDiBZOcpNvM8xnZTfUq7BT4Rs&jump_from=webapi) or open an issue
- Thanks to everyone that contributes to this project

## License

[MIT](http://opensource.org/licenses/MIT)

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

### 13. PK Battle (NEW 🆕)

> PK Battle page: Split-screen with problem description + code editor, real-time countdown at top

<div align="center">
  <p><em>PK Battle in progress — Problem on the left, editor on the right, 20-minute countdown</em></p>
</div>

> PK Match History: View all past battles with opponent info, results, and score changes

<div align="center">
  <p><em>PK History — Shows both players, problem, result, and score delta</em></p>
</div>

### 14. Private Chat (NEW 🆕)

> Chat interface: Contact list on the left, message panel on the right, with unread badges

<div align="center">
  <p><em>Private Chat — Contact list + messaging panel with unread count badges</em></p>
</div>
