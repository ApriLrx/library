# 可视化界面设计（简单版）

## 1. 目标

给现有后端加一个简单的可视化界面，让人不用 curl/Postman 就能完成登录、浏览图书、借阅、收藏、通知，以及管理端的书籍/分类/用户/借阅操作。

## 2. 技术选型

**纯静态页面：HTML + CSS + 原生 JavaScript，无构建工具、无框架、无 CDN 依赖。**

- 文件放到 `library-web/src/main/resources/static/` 下，由 Spring Boot 直接托管。
- 访问 `http://localhost:8080/` 即打开界面，与后端**同源**，无需处理跨域（CORS）。
- 不引入 Vue/React 等框架：一个 CRUD 演示界面用框架反而要加 node 构建链，违背「简单」。

## 3. 文件结构

```text
library-web/src/main/resources/static/
├── index.html          页面骨架与弹窗模板
├── css/style.css       样式
└── js/app.js           请求封装、状态管理、各页面渲染逻辑
```

## 4. 页面与功能

单页应用，登录后左侧菜单 + 右侧内容区，按角色控制菜单显隐。

### 登录页
- 用户名 + 密码登录（`POST /api/auth/login`）。
- 登录成功后把 `token`、`userId`、`role` 存 `localStorage`。
- 支持一键填入预置账号 admin/admin123456 便于测试。

### 读者侧
| 菜单 | 功能 | 对应接口 |
|---|---|---|
| 图书浏览 | 列表（分类/关键词/仅看可借筛选 + 分页）、详情弹窗、借阅（选 7/15/30 天）、收藏 | GET /api/books、GET /api/books/{id}、POST /api/borrows、POST /api/favorites |
| 我的借阅 | 分页 + 状态筛选 | GET /api/borrows/my |
| 我的收藏 | 列表 + 取消收藏 | GET /api/favorites、DELETE /api/favorites/{bookId} |
| 我的通知 | 列表 + 标记已读 | GET /api/notifications、POST /api/notifications/{id}/read |

### 管理侧（admin 登录后显示）
| 菜单 | 功能 | 对应接口 |
|---|---|---|
| 书籍管理 | 列表、新增、编辑、上/下架 | GET/POST/PUT/PATCH /api/books |
| 分类管理 | 列表、新增、编辑、启用/停用 | GET/POST/PUT/PATCH /api/categories |
| 用户管理 | 列表、启用/禁用 | GET /api/users、PATCH /api/users/{id}/status |
| 借阅管理 | 全部借阅、办理归还 | GET /api/borrows、POST /api/borrows/{id}/return |

## 5. 关键实现点

- **请求封装**：统一 `request(method, url, body)`，自动附带 `Authorization: Bearer <token>`；遇到 401 清 token 跳回登录；错误统一 toast 提示。
- **角色控制**：登录接口返回 `role`，据此显示/隐藏「管理」菜单（前端隐藏仅改善体验，安全仍靠后端 `RequireRole` 拦截器）。
- **渲染**：纯前端字符串拼接渲染列表，无框架。

## 6. 发现的接口缺口（影响管理端完整性）

排查接口时发现两处「只能查到启用数据、查不到停用数据」，会导致管理端看不到已下架的书、已停用的分类，无法在界面上重新启用：

1. `GET /api/books` 的 SQL 写死 `WHERE enabled = 1`，无「含下架书」的管理端列表。
2. `GET /api/categories` 只返回启用分类；`CategoryApplicationService.listAll()` 已存在但**没有对应 Controller 接口**。

**两个可选的小后端改动（均为只读、ADMIN 权限）：**
- `GET /api/books/all`：返回含下架书的列表（管理端用）。
- `GET /api/categories/all`：返回含停用分类的列表（管理端用）。

不改的话：管理端只能管理「启用中」的书和分类，已下架/已停用的项目在界面上看不见、无法操作。

## 7. 运行方式

不改后端部署方式：重新构建并启动应用后，浏览器打开 `http://localhost:8080/` 即可使用。

## 8. 默认范围

默认做「读者侧 + 管理侧」完整界面；第 6 节两个只读后端接口是否加，由评审决定（默认加，以补齐管理端功能）。
