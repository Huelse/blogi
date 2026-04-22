# Blogi

一个高度个性的个人博客，支持主题、插件和 AI 总结。

## 技术栈

Server：Java 25、Spring Boot 4、PostgreSQL、HikariCP、MyBatis-Plus、Lombok、Redis、Spring Security 7、JWT

Client：Vue 3、Pinia、Nuxt、TypeScript、Milkdown、Tailwind、Element Plus（管理后台）

Docker Compose 启动基础服务

## 项目结构

```text
.
├── client/                 # Nuxt 4 前端
├── server/                 # Spring Boot 4 后端
├── docker-compose.yml      # PostgreSQL / Redis
├── package.json            # pnpm workspace 入口
└── .env.example            # 本地环境变量模板
```

## 快速开始

### 1. 准备环境

- Node.js 25+
- pnpm 10+
- Java 25
- Docker + Docker Compose

### 2. 初始化环境变量

```bash
cp .env.example .env
```

默认可配置以下连接信息：

- `POSTGRES_HOST` / `POSTGRES_PORT` / `POSTGRES_DB` / `POSTGRES_USER` / `POSTGRES_PASSWORD`
- `REDIS_HOST` / `REDIS_PORT` / `REDIS_PASSWORD`

### 3. 安装前端依赖

```bash
pnpm install
```

### 3.1. 运行前端测试

项目已接入 Nuxt 官方测试方案 `Vitest + @nuxt/test-utils`。

```bash
pnpm test
pnpm test:unit
pnpm test:nuxt
```

### 4. 启动基础服务

```bash
pnpm dev:infra
```

### 5. 启动项目

前端：

```bash
pnpm dev
```

后端：

```bash
pnpm dev:server
```

`pnpm dev:server` 会自动加载仓库根目录 `.env` 中的数据库、Redis 和 JWT 配置。

## 当前初始化内容

- `server` 已提供 Spring Boot 基础入口、健康检查接口 `/api/health`、基础安全配置和 PostgreSQL / Redis 配置。
- `client` 已提供 Nuxt 首页骨架、Tailwind 集成和运行时 API 地址配置。
- `docker-compose.yml` 已提供 PostgreSQL 与 Redis 服务定义。

## TODO

- [x] 用户注册 / 登录 + JWT + 文章 CRUD + Markdown 展示
- [x] 评论功能 + 文章分类 + 文章标签
- [ ] 前端 Vue 3 接入 + 路由守卫
- [ ] 文件上传（头像 / 封面）
- [ ] AI 摘要生成 + Redis 缓存阅读量 / 点赞量
- [ ] 后台管理 + 插件与主题
- [ ] 游客快捷登录留言（浏览器指纹）
- [ ] AI 搜索

第一个 TODO 已完成，当前仓库已包含：

- `/api/auth/register`、`/api/auth/login`、JWT 鉴权
- `/api/posts` 相关创建、编辑、删除、详情与列表接口
- `/api/posts/categories`、`/api/posts/tags`、`/api/posts/{id}/comments` 分类、标签与评论接口
- 前端注册、登录、发文、编辑与 Markdown 渲染页面
- 前端文章分类 / 标签筛选、详情页评论展示与登录用户评论发布
