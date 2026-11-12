# HTTP 方法收敛设计（仅保留 GET / POST）

## 1. 背景与目标

将项目中所有 `PUT` / `PATCH` / `DELETE` 接口统一替换为 `POST`，最终项目只保留 `GET` 与 `POST` 两种 HTTP 方法。

## 2. 现状排查

全项目非 GET/POST 的接口共 6 处，分布在 4 个 Controller：

| # | 文件 | 原方法 | 路径 | 用途 |
|---|---|---|---|---|
| 1 | BookController | PUT | `/api/books/{id}` | 编辑书籍 |
| 2 | BookController | PATCH | `/api/books/{id}/enabled` | 书籍上/下架 |
| 3 | CategoryController | PUT | `/api/categories/{id}` | 编辑分类 |
| 4 | CategoryController | PATCH | `/api/categories/{id}/enabled` | 分类启用/停用 |
| 5 | UserController | PATCH | `/api/users/{id}/status` | 用户启用/禁用 |
| 6 | FavoriteController | DELETE | `/api/favorites/{bookId}` | 取消收藏 |

其余接口均为 GET / POST，无需改动。

## 3. 改动方案（推荐：最小改动）

原则：**路径不变，仅把注解从 PUT/PATCH/DELETE 换成 POST**。请求体、DTO、Service、鉴权逻辑全部不变。

| # | 文件 | 原注解 | 新注解 | 参数/请求体 |
|---|---|---|---|---|
| 1 | BookController | `@PutMapping("/{id}")` | `@PostMapping("/{id}")` | 不变 |
| 2 | BookController | `@PatchMapping("/{id}/enabled")` | `@PostMapping("/{id}/enabled")` | 不变 |
| 3 | CategoryController | `@PutMapping("/{id}")` | `@PostMapping("/{id}")` | 不变 |
| 4 | CategoryController | `@PatchMapping("/{id}/enabled")` | `@PostMapping("/{id}/enabled")` | 不变 |
| 5 | UserController | `@PatchMapping("/{id}/status")` | `@PostMapping("/{id}/status")` | 不变 |
| 6 | FavoriteController | `@DeleteMapping("/{bookId}")` | `@PostMapping("/{bookId}")` | 不变 |

同时删除 4 个 Controller 中不再使用的 import：`PutMapping`、`PatchMapping`、`DeleteMapping`。

## 4. 影响面与兼容性

- 仅 HTTP 方法与注解变化，请求体结构、DTO、Service、仓储、鉴权逻辑均不变。
- 鉴权拦截器 `AuthInterceptor` 无需改动：读接口仍是 GET（白名单），写接口仍走 role 校验。
- 改动后接口清单：
  - `POST /api/books/{id}` —— 编辑书籍
  - `POST /api/books/{id}/enabled` —— 书籍上/下架
  - `POST /api/categories/{id}` —— 编辑分类
  - `POST /api/categories/{id}/enabled` —— 分类启用/停用
  - `POST /api/users/{id}/status` —— 用户启用/禁用
  - `POST /api/favorites/{bookId}` —— 取消收藏

## 5. 备选方案（未采用，供评审）

将「编辑」类接口改为 `POST /{id}/update`、删除收藏改为 `POST /{bookId}/remove` 等带动作后缀的路径，语义更直观，但改动更大、偏离「仅替换方法」的诉求。如需更明确的语义可切换到此方案。

## 6. 文档同步（可选）

`readme.md` 接口清单仍标注 PUT/PATCH/DELETE，且其路径（`/api/admin/...`）与当前代码不一致。可在代码改动后另行同步文档，本次默认不改 readme，除非评审要求。
