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
- [ ] 前端国际化 + 路由守卫
- [x] 文件上传（头像 / 封面）
- [x] AI 摘要生成 + Redis 缓存阅读量 / 点赞量
- [ ] 后台管理 + 插件与主题
- [x] 游客快捷登录留言（浏览器指纹）
- [ ] 搜索和RSS订阅

第一个 TODO 已完成，当前仓库已包含：

- `/api/auth/register`、`/api/auth/login`、JWT 鉴权
- `/api/posts` 相关创建、编辑、删除、详情与列表接口
- `/api/posts/categories`、`/api/posts/tags`、`/api/posts/{id}/comments` 分类、标签与评论接口
- 前端注册、登录、发文、编辑与 Markdown 渲染页面
- 前端文章分类 / 标签筛选、详情页评论展示与登录用户评论发布

## 后续开发里程碑

以下拆解基于当前仓库现状，用于把 README 中未完成的 TODO 进一步落到可执行任务。

### M1. 前端国际化 + 路由守卫

- [x] 接入 Nuxt 国际化方案，为首页、登录、注册、后台、评论等页面抽离文案
- [x] 增加语言切换入口，并支持至少中英文两套文案
- [x] 将 `/admin`、`/admin/posts/*`、`/admin/settings` 的登录校验改为统一路由中间件
- [x] 统一处理未登录跳转和登录后回跳逻辑，避免在页面内重复编写 `navigateTo`

### M2. 文件上传（头像 / 封面）

- [x] 新增上传接口，支持 `multipart/form-data`
- [x] 支持本地磁盘、阿里云OSS、腾讯云COS、亚马逊S3和WebDAV等多种存储方式
- [x] 为文章增加封面上传能力，并在前端编辑器中支持选择、预览和回填
- [x] 为用户或游客资料增加头像上传能力
- [x] 设计文件存储策略：本地磁盘或对象存储
- [x] 增加文件类型、大小、安全校验和访问 URL 规则
- [x] 为 `posts` 增加 `cover_url` 字段，为 `users` / `visitors` 增加 `avatar_url` 字段

### M3. AI 摘要生成 + Redis 缓存阅读量 / 点赞量

- [x] 接入 AI 服务，为文章生成摘要
- [x] 在发文和编辑页面增加“AI 生成摘要”按钮、加载态和失败降级提示
- [x] 保留当前基于正文截断的摘要逻辑，作为 AI 不可用时的降级方案
- [x] 增加文章阅读量展示，并使用 Redis 进行计数或缓存
- [x] 为点赞量增加 Redis 缓存读取与失效策略
- [x] 设计阅读量去重策略，避免短时间重复刷新造成异常计数
- [x] 为阅读量持久化补充数据库字段和回写机制

### M4. 后台管理 + 插件与主题

- [ ] 在现有后台基础上补充分类管理、标签管理、评论管理和站点设置
- [ ] 将 `site_settings` 从单一 `footer_html` 扩展为通用 key-value 配置中心
- [ ] 增加更多前台可配置项，例如站点标题、描述、页脚、导航和首页布局
- [ ] 设计主题系统最小协议：主题配置、样式变量、模板扩展点
- [ ] 设计插件系统最小协议：注册点、配置结构、启停状态和加载流程
- [ ] 为后台增加主题和插件管理入口，而不只停留在当前的基础设置页

### M5. 搜索和 RSS 订阅

- [ ] 为首页增加关键词搜索入口
- [ ] 为文章列表接口增加标题、摘要、正文等字段的关键词搜索能力
- [ ] 评估数据库搜索实现方案，必要时补全文索引或引入专用搜索方案
- [ ] 新增 RSS/Atom 订阅输出接口，例如 `/rss.xml` 或 `/feed.xml`
- [ ] 为 RSS 配置站点 URL、文章摘要、发布时间和作者等必要字段
- [ ] 在前台页脚、导航或关于页提供 RSS 订阅入口

## 推荐开发顺序

1. 先完成 `M1. 前端国际化 + 路由守卫` 和 `M5. 搜索和 RSS 订阅`，改动相对集中，收益也更直接。
2. 再完成 `M2. 文件上传（头像 / 封面）`，因为这会影响数据库结构、表单流程和资源访问策略。
3. 接着完成 `M3. AI 摘要生成 + Redis 缓存阅读量 / 点赞量`，这部分依赖外部 AI 服务和缓存策略设计。
4. 最后完成 `M4. 后台管理 + 插件与主题`，这部分更偏架构设计，适合在基础能力稳定后推进。
