# 书籍封面上传功能设计

## 1. 目标

在书籍管理（新增/编辑）中支持上传封面图片，并在列表/详情中展示封面缩略图。

## 2. 现状

- `books.cover_url`（VARCHAR 500）已存在，当前仅存 URL 字符串。
- 无文件上传/存储基础设施，`pom.xml` 无 OSS/S3 依赖。
- 前端书籍表单里 `封面URL` 是纯文本输入框。

## 3. 方案（推荐：本地磁盘）

### 存储
- 上传目录可配置 `library.upload.dir`，默认 `./uploads`。
- 文件保存为 `{dir}/covers/{uuid}.{ext}`。
- 通过静态资源映射 `/uploads/**` → 本地目录对外提供访问，浏览器直接加载图片。
- 复用现有 `coverUrl` 字段，**不新增数据库字段**。

### 后端改动
1. `application.yml`：加 multipart 大小限制 + 上传目录配置。
   - `spring.servlet.multipart.max-file-size: 5MB`
   - `spring.servlet.multipart.max-request-size: 6MB`
   - `library.upload.dir: ./uploads`
2. `WebConfig`：新增 `addResourceHandlers("/uploads/**" → file:上传目录/)`，公开可访问（不经过 `/api/**` 鉴权拦截）。
3. 新增 `FileStorageService`（放 `library-infra`，属于基础设施）：
   - 校验文件非空、扩展名/Content-Type 白名单（jpg/jpeg/png/gif/webp）、大小 ≤ 5MB。
   - UUID 文件名防覆盖与路径穿越，落盘后返回访问 URL。
4. 新增 `FileController`：
   - `POST /api/files/cover`（ADMIN，multipart 参数 `file`）→ 返回 `{ "url": "/uploads/covers/xxx.jpg" }`。
5. `GlobalExceptionHandler`：新增对 `MaxUploadSizeExceededException` 的处理，返回 400「文件过大」而非 500。

### 前端改动
- 书籍新增/编辑表单：`封面URL` 输入框旁加「上传封面」按钮 → 选图 → 上传 → 自动回填 URL 并显示缩略图预览。
- 书籍列表与详情：显示封面缩略图（无封面时显示占位）。

### 安全/健壮性
- 扩展名 + Content-Type 双重白名单校验。
- 大小限制（配置化 + 服务内手动校验）。
- 上传接口要求 ADMIN 角色，与书籍管理一致。

## 4. 备选方案（对象存储 OSS/S3，暂不建议）

存储到阿里云 OSS / S3 等对象存储，`coverUrl` 存对象存储 key 或带签名 URL。需要额外的账号、密钥与 SDK 依赖，与「简单本地演示」目标不符；如后续要上云可平滑迁移（替换 `FileStorageService` 实现即可）。

## 5. 决策点

1. 存储方式：本地磁盘（推荐） vs 对象存储。
2. 封面展示范围：列表/详情均显示缩略图（推荐） vs 仅在编辑表单显示。
