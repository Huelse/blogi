# M3 使用说明：AI 摘要与阅读/点赞缓存

本文档说明 M3 功能的接口、配置和运行行为。

## 1. 环境变量

在仓库根目录 `.env` 中配置：

```env
AI_ENABLED=false
AI_BASE_URL=https://api.openai.com/v1
AI_API_KEY=
AI_MODEL=gpt-4o-mini
AI_SUMMARY_MAX_CHARS=220
```

说明：

- `AI_ENABLED`：`true` 时才会尝试调用 AI。
- `AI_API_KEY`：为空或不可用时，会自动走摘要降级逻辑。
- `AI_SUMMARY_MAX_CHARS`：AI 摘要最大长度（最终仍受后端字段长度约束）。

Redis 仍使用现有配置：

```env
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=blogi
```

## 2. AI 摘要接口

### `POST /api/posts/summary/generate`

- 鉴权：需要登录（Bearer Token）。
- 用途：在发文/编辑页面点击“AI 生成摘要”时调用。

请求体：

```json
{
  "title": "文章标题",
  "contentMarkdown": "# 正文\n\n..."
}
```

响应体（`data`）：

```json
{
  "summary": "生成后的摘要文本",
  "generatedByAi": true
}
```

降级行为：

- 当 AI 未启用、Key 缺失、请求失败或返回为空时：
  - `generatedByAi=false`
  - `summary` 使用现有“正文截断摘要”逻辑生成

## 3. 阅读量接口

### `POST /api/posts/{postId}/views`

- 鉴权：公开接口（无需登录）。
- 用途：文章详情页加载后上报一次阅读。
- 去重：同一 `postId + fingerprintHash` 在 10 分钟窗口内只计一次。

请求体：

```json
{
  "fingerprintHash": "64位十六进制字符串"
}
```

响应体（`data`）：

```json
{
  "viewCount": 12,
  "counted": true
}
```

说明：

- `counted=true`：本次上报被计入阅读量。
- `counted=false`：命中去重窗口，仅返回当前阅读量。

## 4. 点赞量缓存行为

点赞相关接口保持不变：

- `GET /api/posts/{postId}/likes`
- `POST /api/posts/{postId}/likes`
- `DELETE /api/posts/{postId}/likes`

缓存策略：

- 点赞数读取时优先使用 Redis 缓存。
- 点赞/取消点赞后会主动失效对应文章的点赞缓存，下一次读取自动回填。

## 5. 阅读量缓存与持久化行为

### 数据库字段

- `posts.view_count`：持久化阅读量总数（`BIGINT NOT NULL DEFAULT 0`）。

### Redis 中间状态

- 阅读量缓存键：`post:stats:views:count:{postId}`
- 待回写增量键：`post:stats:views:pending:{postId}`
- 去重键：`post:stats:views:dedup:{postId}:{fingerprintHash}`

### 回写机制

- 应用内定时任务每 60 秒扫描 `pending` 键并回写 DB（可通过配置修改周期）。
- 当某文章 `pending` 增量达到批量阈值（默认 20）时会触发提前回写。
- Redis 不可用时自动降级为直接写数据库计数。

## 6. 前端接入点

- 发文/编辑页：`client/components/PostEditorForm.vue`
  - 新增“AI 生成摘要”按钮、加载态、失败提示、降级提示。
- 文章详情页：`client/pages/posts/[id].vue`
  - 加载时调用 `/views` 上报阅读。
  - 显示 `viewCount`。
- 首页列表：`client/pages/index.vue`
  - 显示文章 `viewCount`。

## 7. 验证建议

后端：

```bash
cd server
mvn test
```

前端：

```bash
pnpm lint
```

如果本地要手工验证：

1. 打开任意文章详情页，观察阅读量增加。
2. 10 分钟内刷新同一浏览器页，确认阅读量不连续增长。
3. 在后台发文页点击“AI 生成摘要”，分别验证 AI 可用与不可用时的返回行为。
