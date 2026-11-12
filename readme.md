# 图书管理系统

# **项目启动与调试**

## **环境要求**

- JDK 8

- Maven 3\.6\+

- MySQL 8\.0

## **启动步骤**

1. **初始化数据库（首次）**：在 MySQL 中依次执行 `db/schema.sql`（建表）与 `db/data.sql`（预置数据）。

2. **改配置（如需要）**：编辑 `library-web/src/main/resources/application.yml`，按需修改数据源账号密码（默认 `root/123456`）与服务端口（默认 8080）。

3. **启动后端**：

    - 命令行：`mvn clean package -DskipTests` 后执行 `java -jar library-web/target/library-web-0.0.1-SNAPSHOT.jar`

    - 或 IDEA：直接运行 `library-web` 模块的 `LibraryWebApplication`

4. **打开可视化界面**：浏览器访问 [http://localhost:8080/](http://localhost:8080/)

## **预置账号**

|角色|账号|密码|
|---|---|---|
|管理员|admin|admin123456|

> 读者账号可在界面「注册读者」处自助注册（用户名 5\-50 位，密码 10\-20 位且同时含字母与数字）。
> 
> 

## **调试说明**

- 后端与可视化界面**同源部署**：界面为 `library-web/src/main/resources/static/` 下的静态文件，由 Spring Boot 直接托管，无需单独起前端，无跨域问题。

- 接口统一前缀 `/api`，返回结构 `{code, message, data}`，`code=0` 表示成功。

- 登录后把返回的 `token` 放入请求头 `Authorization: Bearer <token>`（可视化界面已自动处理）。

- 封面上传：`POST /api/files/cover`（ADMIN，multipart 参数 `file`），图片默认存 `./uploads` 目录，通过 `/uploads/**` 访问。



# 系统概述

实现一个电子图书管理系统，主要功能包括

- 浏览书籍

- 查阅书籍详细资料

- 借阅书籍

- 归还书籍

- 查看目前已借阅的书籍



# 概要设计

## 角色设计

系统中主要存在两个角色：管理员与用户

### 图书管理员\(Admin\)

admin的权限包括：

1. 书籍分类管理

    - 添加、删除、查询书籍分类

    - 编辑分类信息

2. 图书管理

    - 添加、删除、查询书籍

    - 编辑书籍基本信息

    - 管理书籍库存

3. 用户管理

    - 查询用户信息

    - 修改用户状态

4. 借阅管理

    - 查询借阅记录

    - 归还图书

### 普通读者\(User\)

user的权限包括： 

1. 搜索图书

2. 浏览图书

3. 借阅图书

4. 查看借阅明细

5. 管理个人信息

## 数据库设计

1. **图书分类 categories**

|字段名称|类型|约束|Default|含义|
|---|---|---|---|---|
|id|bigint|AUTO\_INCREMENT<br>PRIMARY KEY||主键|
|name|varchar\(100\)|NOT NULL<br>UNIQUE KEY||分类名称|
|enabled|tinyint|NOT NULL|1|是否启用。停用后该分类不再展示，其下图书不做级联变更|
|created\_at|datetime|NOT NULL|CURRENT\_TIMESTAMP|创建时间|
|updated\_at|datetime|NOT NULL|CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP|更新时间|

2. **图书表 books**

|字段名称|类型|约束|Default|含义|
|---|---|---|---|---|
|id|bigint|AUTO\_INCREMENT<br>PRIMARY KEY||主键|
|isbn|varchar\(20\)|NOT NULL<br>UNIQUE KEY||国际标准书号，用于录入查重|
|name|varchar\(200\)|NOT NULL||书名，建普通索引以支持前缀检索|
|author|varchar\(200\)|NOT NULL||作者，多作者用逗号分隔|
|publisher|varchar\(100\)|||出版社|
|publish\_year|smallint|||出版年份|
|description|text|||内容概述|
|cover\_url|varchar\(500\)|||封面地址，存对象存储的 key 或带签名 URL|
|category\_id|bigint|NOT NULL||所属分类ID，逻辑外键|
|total\_count|int|NOT NULL|0|馆藏总量|
|available\_count|int|NOT NULL|0|可借数量，借阅时原子扣减、归还时回加|
|enabled|tinyint|NOT NULL|1|是否上架，下架后不对读者展示、不可借阅|
|created\_at|datetime|NOT NULL|CURRENT\_TIMESTAMP|创建时间|
|updated\_at|datetime|NOT NULL|CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP|更新时间|

3. **图书册 book\_copy**

同一书目下的每一本实体书单独成行，用于记录条码、在借状态和损坏/丢失情况；借阅记录指向具体册，而不是只指向书目。

|字段名称|类型|约束|Default|含义|
|---|---|---|---|---|
|id|bigint|AUTO\_INCREMENT<br>PRIMARY KEY||主键|
|book\_id|bigint|NOT NULL||所属书目，逻辑外键|
|barcode|varchar\(50\)|NOT NULL<br>UNIQUE KEY||册条码|
|status|varchar\(20\)|NOT NULL|AVAILABLE|AVAILABLE 可借 / BORROWED 在借 / LOST 丢失 / DAMAGED 损坏|
|created\_at|datetime|NOT NULL|CURRENT\_TIMESTAMP|创建时间|
|updated\_at|datetime|NOT NULL|CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP|更新时间|

4. **users**

|字段名称|类型|约束|Default|含义|
|---|---|---|---|---|
|id|bigint|AUTO\_INCREMENT<br>PRIMARY KEY||主键|
|user\_name|varchar\(50\)|NOT NULL<br>UNIQUE KEY||用户名|
|password|varchar\(100\)|NOT NULL||密码哈希，存 bcrypt/argon2 结果，不存明文|
|role|varchar\(20\)|NOT NULL|READER|READER / ADMIN|
|status|tinyint|NOT NULL|1|1 正常 / 0 禁用。禁用后不能登录与借阅，不影响历史记录|
|created\_at|datetime|NOT NULL|CURRENT\_TIMESTAMP|创建时间|
|updated\_at|datetime|NOT NULL|CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP|更新时间|

5. **借阅记录 borrow\_record**

|字段名称|类型|约束|Default|含义|
|---|---|---|---|---|
|id|bigint|AUTO\_INCREMENT<br>PRIMARY KEY||主键|
|user\_id|bigint|NOT NULL||借阅人，逻辑外键|
|book\_id|bigint|NOT NULL||书目 id，逻辑外键，冗余字段，便于按书目统计|
|copy\_id|bigint|NOT NULL||实际借出的册|
|borrow\_date|datetime|NOT NULL||借出时间|
|expected\_due\_date|datetime|NOT NULL||应还时间，等于借出时间加借期|
|returned\_at|datetime|||实际归还时间，为空表示尚未归还|
|status|varchar\(20\)|NOT NULL|BORROWED|BORROWED 在借 / RETURNED 已还 / OVERDUE 逾期 / LOST 丢失|
|renew\_count|tinyint|NOT NULL|0|已续借次数|
|operator\_id|bigint|||办理归还的管理员 id，用于审计|
|created\_at|datetime|NOT NULL|CURRENT\_TIMESTAMP|创建时间|
|updated\_at|datetime|NOT NULL|CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP|更新时间|

1. **收藏 favorite**

|字段名称|类型|约束|Default|含义|
|---|---|---|---|---|
|id|bigint|AUTO\_INCREMENT<br>PRIMARY KEY||主键|
|user\_id|bigint|NOT NULL<br>FOREIGN KEY → users\(id\)||收藏人，只记录关系|
|book\_id|bigint|NOT NULL<br>FOREIGN KEY → books\(id\)||被收藏的书目|
|created\_at|datetime|NOT NULL|CURRENT\_TIMESTAMP|收藏时间|
|updated\_at|datetime|NOT NULL|CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP|更新时间|

同一读者对同一书目最多一条收藏，由 `uk_fav_user_book(user_id, book_id)` 保证；书目下架后收藏保留，但不再触发到书提醒。

2. **通知 notification**

|字段名称|类型|约束|Default|含义|
|---|---|---|---|---|
|id|bigint|AUTO\_INCREMENT<br>PRIMARY KEY||主键|
|user\_id|bigint|NULL||接收人，为空表示全站公告|
|book\_id|bigint|NULL||相关书目，可为空|
|borrow\_record\_id|bigint|NULL||关联的借阅记录，用于到期提醒与催还|
|type|varchar\(20\)|NOT NULL||DUE\_REMIND 到期提醒 / OVERDUE 逾期 / AVAILABLE 可借提醒 / SYSTEM 系统公告|
|content|varchar\(1000\)|NOT NULL||通知正文|
|read\_at|datetime|||已读时间，为空表示未读|
|created\_at|datetime|NOT NULL|CURRENT\_TIMESTAMP|创建时间|
|updated\_at|datetime|NOT NULL|CURRENT\_TIMESTAMP ON UPDATE CURRENT\_TIMESTAMP|更新时间|

**索引设计**

|表|索引|用途|
|---|---|---|
|books|UNIQUE\(isbn\)<br>INDEX\(name\)<br>INDEX\(category\_id, enabled\)|录入查重；书名检索；按分类拉取在架图书|
|book\_copy|UNIQUE\(barcode\)<br>INDEX\(book\_id, status\)|条码唯一；分配可借册、统计在借数量|
|borrow\_record|UNIQUE\(user\_id, active\_book\_id\)<br>INDEX\(user\_id, status\)<br>INDEX\(book\_id\)<br>INDEX\(status, expected\_due\_date\)|借阅幂等；我的借阅；书目借阅统计；到期与逾期扫描|
|favorite|UNIQUE\(user\_id, book\_id\)|防重复收藏；按用户列收藏|
|notification|INDEX\(user\_id, read\_at\)<br>INDEX\(borrow\_record\_id, type\)|未读通知查询；到期提醒去重|

列表类接口统一分页（page、pageSize，默认 20 条），借阅记录与书籍列表都必须分页返回。

## 业务规则

1. 用户注册

    - 账户类型：注册的用户均为普通用户，管理员用户只能后台添加

    - 账户名：5\-50个字符，不允许出现重复的账户名

2. 管理分类\(admin\)

    - 其他分类：存放未分类或者分类未生效的图书

3. 管理书籍\(admin\)

    - 创建书籍：管理员可以从后台手动添加书籍并调整库存

    - 下架书籍：管理员可以下架书籍，被下架的书籍不展示给用户

    - 删除书籍：只允许下架，不允许删除

    - 副本创建：原则上应由线下录入，为简化操作改为自动生成

4. 书籍浏览

    - 预览页面：书籍封面、书籍名称、作者

    - 详细信息：其他介绍、内容概述

5. 书籍借阅

    - 借阅条件：只有库存\>0的图书才可以借阅，借阅后库存\-1

    - 借阅期限：7、15、30天三个档位可选

    - 归还提醒：借阅到期的前一天系统会发送通知提醒归还

6. 书籍归还\(admin\)

    - 用户线下归还书籍后，管理员在页面上手动修改归还状态，系统自动恢复库存

## 架构设计

### 接口层\(web\)

- 负责设计对外HTTP接口，包含启动类

- 负责鉴权的调用

- 实体：对外展示为VO，接收参数为DTO

- 校验HTTP参数是否合法

### 业务层\(biz\)

- 负责业务规则前置校验

    - 存在性校验

    - 格式校验

- 负责跨领域任务编排与事务，主要包括

    - 借阅：协调用户领域\(用户状态\)、书籍领域\(书籍库存\)、借阅领域\(借阅记录\)

    - 归还操作：协调书籍领域\(书籍库存\)与借阅领域\(借阅记录\)

- 定义定时任务与事件监听

    - 定时任务：每日扫描「到期前一天提醒」与「逾期标记 + 通知」

    - 事件监听：归还副本可借后异步通知收藏该书的读者

### 领域层\(domain\)

- 业务逻辑核心层

- 负责数据库交互\(Repository\)的定义

- 负责对数据进行业务性校验

    - 库存不能为负数

    - 书目必须可用

- 禁止出现跨领域调用

#### 书籍领域\(books\)

负责书目、副本与分类的相关操作

**功能**

- 数据库操作：所有分类、书目、副本相关的增删改查

- 书目：库存操作、封面上传

- 书目副本：创建书目时自动生成副本

#### 用户领域\(user\)

负责账号的建立、凭证校验、身份状态维护，以及读者收藏关系的记录。

**功能**

- 数据库操作：所有用户\&收藏相关的增删改查

- 登录：校验账户状态

#### 借阅领域\(borrow\)

负责借阅记录相关操作

**功能**

- 数据库操作：借阅记录的增删改查

- 借阅：生成借阅记录

- 归还：修改借阅记录状态为已归还

#### 通知领域\(notification\)

负责站内信通知的记录与已读管理

**功能**

- 数据库操作：通知的增删改查与已读更新

- 通知生成：可借提醒（异步）、到期提醒与逾期（定时任务）、系统公告（管理员广播）

### 基础设施层\(infra\)

- 负责鉴权（JWT 与拦截器）、文件存储、全局异常处理等实现

- 负责数据库连接的实现

- 负责鉴权逻辑\&拦截器的实现

#### 数据库模块\(storage\)

- 逻辑上属于infra模块，为便于维护拆分成单独模块。

- 负责数据库交互的实现

### common模块

存放部分枚举、常量、HHTP返回对象、异常定义等

# 详细设计

## 数据库 DDL

以下语句以 MySQL 8\.0、InnoDB、utf8mb4 为准，建表顺序按外键依赖自上而下执行。

```sql
-- 1. 图书分类
CREATE TABLE categories (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name        VARCHAR(100)    NOT NULL COMMENT '分类名称',
  enabled     TINYINT         NOT NULL DEFAULT 1 COMMENT '1 启用 / 0 停用',
  created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_categories_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类';

-- 2. 书目
CREATE TABLE books (
  id              BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  isbn            VARCHAR(20)     NOT NULL COMMENT 'ISBN，录入查重',
  name            VARCHAR(200)    NOT NULL COMMENT '书名',
  author          VARCHAR(200)    NOT NULL COMMENT '作者，多作者逗号分隔',
  publisher       VARCHAR(100)        NULL COMMENT '出版社',
  publish_year    SMALLINT            NULL COMMENT '出版年份',
  description     TEXT                NULL COMMENT '内容概述',
  cover_url       VARCHAR(500)        NULL COMMENT '封面对象存储地址',
  category_id     BIGINT UNSIGNED NOT NULL COMMENT '所属分类',
  total_count     INT             NOT NULL DEFAULT 0 COMMENT '馆藏总量',
  available_count INT             NOT NULL DEFAULT 0 COMMENT '可借数量',
  enabled         TINYINT         NOT NULL DEFAULT 1 COMMENT '1 上架 / 0 下架',
  created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_books_isbn (isbn),
  KEY idx_books_name (name),
  KEY idx_books_category (category_id, enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='书目';

-- 3. 图书册
CREATE TABLE book_copy (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  book_id     BIGINT UNSIGNED NOT NULL COMMENT '所属书目',
  barcode     VARCHAR(50)     NOT NULL COMMENT '册条码',
  status      VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE' COMMENT 'AVAILABLE/BORROWED/LOST/DAMAGED',
  created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_copy_barcode (barcode),
  KEY idx_copy_book_status (book_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书册';

-- 4. 用户
CREATE TABLE users (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_name   VARCHAR(50)     NOT NULL COMMENT '用户名',
  password    VARCHAR(100)    NOT NULL COMMENT '密码哈希，bcrypt/argon2',
  role        VARCHAR(20)     NOT NULL DEFAULT 'READER' COMMENT 'READER/ADMIN',
  status      TINYINT         NOT NULL DEFAULT 1 COMMENT '1 正常 / 0 禁用',
  created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_name (user_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- 5. 借阅记录
CREATE TABLE borrow_record (
  id                BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id           BIGINT UNSIGNED NOT NULL COMMENT '借阅人',
  book_id           BIGINT UNSIGNED NOT NULL COMMENT '书目，冗余便于按书目统计',
  copy_id           BIGINT UNSIGNED NOT NULL COMMENT '实际借出的册',
  borrow_date       DATETIME        NOT NULL COMMENT '借出时间',
  expected_due_date DATETIME        NOT NULL COMMENT '应还时间',
  returned_at       DATETIME            NULL COMMENT '实际归还时间',
  status            VARCHAR(20)     NOT NULL DEFAULT 'BORROWED' COMMENT 'BORROWED/RETURNED/OVERDUE/LOST',
  renew_count       TINYINT         NOT NULL DEFAULT 0 COMMENT '已续借次数',
  operator_id       BIGINT UNSIGNED     NULL COMMENT '归还经办人',
  created_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  active_book_id    BIGINT UNSIGNED     NULL GENERATED ALWAYS AS (
                      CASE WHEN status IN ('BORROWED','OVERDUE') THEN book_id ELSE NULL END
                    ) STORED COMMENT '活动借阅书目 id（生成列，用于借阅幂等唯一约束）',
  PRIMARY KEY (id),
  UNIQUE KEY uk_borrow_active_user_book (user_id, active_book_id),
  KEY idx_borrow_user_status (user_id, status),
  KEY idx_borrow_book (book_id),
  KEY idx_borrow_due (status, expected_due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='借阅记录';

-- 6. 收藏
CREATE TABLE favorite (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id     BIGINT UNSIGNED NOT NULL,
  book_id     BIGINT UNSIGNED NOT NULL,
  created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_fav_user_book (user_id, book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏';

-- 7. 通知
CREATE TABLE notification (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id          BIGINT UNSIGNED     NULL COMMENT '接收人，NULL 为全站公告',
  book_id          BIGINT UNSIGNED     NULL COMMENT '相关书目',
  borrow_record_id BIGINT UNSIGNED     NULL COMMENT '关联借阅记录',
  type             VARCHAR(20)     NOT NULL COMMENT 'DUE_REMIND/OVERDUE/AVAILABLE/SYSTEM',
  content          VARCHAR(1000)   NOT NULL COMMENT '通知内容',
  read_at          DATETIME            NULL COMMENT '已读时间，为空表示未读',
  created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_notify_user_read (user_id, read_at),
  KEY idx_notify_borrow_type (borrow_record_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知';
```

## 领域设计

本节按领域整理包结构、聚合与主要类、状态机与不变式。仓储接口定义在各领域包内，实现放在基础设施层；事务边界留在应用层。

### 书籍领域\(books\)

**包与类**

```text
domain/books/
├── model/
│   ├── Book.java              聚合根，持有副本集合与可借数量
│   ├── Isbn.java              值对象
│   ├── BookInfo.java          值对象：书名、作者、出版社、简介
│   ├── BookCopy.java          实体：条码、状态
│   ├── CopyStatus.java        枚举：AVAILABLE / BORROWED / LOST / DAMAGED
│   └── Category.java          聚合根
├── service/
│   └── BookStockService.java    领域服务：借出/归还副本的聚合变更与持久化
├── repository/
│   ├── BookRepository.java
│   ├── BookQuery.java
│   └── CategoryRepository.java
└── event/
    └── CopyAvailableEvent.java    副本可借事件，触发收藏通知
```

**聚合与类职责**

- Book：书目信息、可借数量与副本集合的唯一出入口；`lend()` 自选可借副本并扣减库存，`giveBack(copyId)` 回加库存。

- BookCopy：条码与状态，随 Book 聚合一起持久化，不单独对外暴露仓储。

- Category：独立聚合，只管分类名称与启停。

- BookStockService：封装「借出 / 归还副本」的聚合状态流转与持久化。

**不变式**

- 0 ≤ available\_count ≤ total\_count，且 available\_count = total\_count − 状态为 BORROWED 的副本数。

- 副本状态为 BORROWED 时，必须存在一条未归还的借阅记录指向它。

- 下架只影响展示与借阅，不改变副本状态和已有记录。

### 用户领域\(user\)

**包与类**

```text
domain/user/
├── model/
│   ├── User.java              聚合根
│   ├── UserName.java          值对象，含 5-50 字符规则
│   ├── Password.java          值对象，只持有哈希值
│   ├── Role.java              枚举：READER / ADMIN
│   └── UserStatus.java        枚举：ACTIVE / DISABLED
├── service/
│   ├── PasswordEncoder.java   密码哈希接口
│   ├── PasswordPolicy.java    领域服务：密码强度校验
│   └── UserService.java       领域服务：凭证校验
├── repository/
│   └── UserRepository.java
└── event/
    └── UserRegisteredEvent.java
```

**聚合与类职责**

- User：账号、角色与状态的唯一入口，`disable()` / `enable()` 控制账号可用性。

- UserName、Password：值对象，在构造时完成格式与长度校验，非法值无法创建对象。

- PasswordPolicy：密码强度规则（10\-20 字符、字母与数字），与存储实现无关。

- UserService：凭证校验（密码匹配且账号状态正常）。

**不变式**

- 用户名全局唯一且大小写不敏感；密码只存哈希，不可逆。

- 禁用账号不改变历史借阅记录，只影响登录与新的借阅。

- 注册只能创建 READER。

### 收藏领域\(favorite\)

**包与类**

```text
domain/favorite/
├── model/
│   └── Favorite.java          实体：读者与书目的收藏关系
└── repository/
    └── FavoriteRepository.java
```

**聚合与类职责**

- Favorite：读者与书目的收藏关系，`bookName`/`author` 为列表查询 join 的冗余展示字段。

**不变式**

- 同一读者对同一书目最多一条收藏（唯一索引 uk\_fav\_user\_book）。

### 借阅领域\(borrow\)

**包与类**

```text
domain/borrow/
├── model/
│   ├── BorrowRecord.java      聚合根
│   ├── BorrowStatus.java      枚举：BORROWED / RETURNED / OVERDUE / LOST
│   ├── BorrowPeriod.java      值对象：7 / 15 / 30 天
│   └── DueDate.java           值对象：应还时间计算
├── service/
│   ├── BorrowService.java     领域服务：借阅资格校验与记录生命周期
│   └── OverduePolicy.java     领域服务：逾期判定
├── repository/
│   └── BorrowRecordRepository.java
└── event/
    ├── BorrowCreatedEvent.java
    └── BookReturnedEvent.java
```

**聚合与类职责**

- BorrowRecord：一次借阅的完整生命周期，`returnBack()` 校验状态后流转为已还，`markOverdue()`、`markLost()` 只允许从在借或逾期进入。

- BorrowPeriod：7 / 15 / 30 三个取值，构造即校验，其他天数无法创建。

- DueDate：由借出时间与借期计算应还时间，逾期判定不依赖数据库字段。

- BorrowService：借阅资格校验（未重复借阅、期限合法）与借阅记录的创建 / 归还。

- OverduePolicy：给定当前时间判断是否逾期，供定时任务与查询复用。

**状态机**

|当前状态|触发|目标状态|校验|
|---|---|---|---|
|BORROWED|管理员办理归还|RETURNED|记录未归还、对应册为 BORROWED|
|BORROWED|定时任务扫描到已过应还日期|OVERDUE|returned\_at 为空且 due\_date 已过|
|OVERDUE|管理员办理归还|RETURNED|同上，允许逾期归还|
|BORROWED / OVERDUE|管理员登记丢失|LOST|册状态置 LOST，库存不回加|

**关键约束**

- 借阅前校验：用户 status=1、书目 enabled=1、available\_count\>0、该用户对该书目无未归还记录。

- 扣减与占册必须放在同一事务；任一步失败整体回滚，不允许出现"扣了库存但没生成记录"。

- 归还是状态流转而非单纯改字段，重复调用返回业务异常 40902，不得二次回加库存。

- 到期/逾期提醒由通知应用服务的定时任务扫描借阅记录生成，借阅领域不感知通知通道与已读。

### 通知领域\(notification\)

**包与类**

```text
domain/notification/
├── model/
│   ├── Notification.java      聚合根：一条站内信通知
│   └── NotificationType.java  枚举：DUE_REMIND / OVERDUE / AVAILABLE / SYSTEM
└── repository/
    └── NotificationRepository.java
```

**聚合与类职责**

- Notification：通知的归属与已读，`markReadBy(userId)` 校验归属后标记已读；`user_id` 为空表示全站公告（公告不追踪个人已读）。

- NotificationType：DUE\_REMIND 到期提醒 / OVERDUE 逾期 / AVAILABLE 可借提醒 / SYSTEM 系统公告。

**关键约束**

- 通知生成在应用层（biz）编排：可借提醒由归还事件异步触发，到期/逾期由定时任务扫描生成，公告由管理员广播。

- 异步采用 Spring 事件 + `@Async` 线程池（进程内），不引入消息队列；归还成功（事务提交）后才异步发可借提醒。

- 到期提醒幂等：同一借阅记录只生成一条 DUE\_REMIND（按 borrow\_record\_id \+ type 去重）；逾期靠状态流转（BORROWED→OVERDUE）天然幂等。

## 响应设计

所有接口返回同一结构：code、message、data 三个字段，code=0 表示成功；错误码按业务分类集中定义，并与 HTTP 状态码对应；时间统一 ISO 8601（东八区），列表数据放在 data\.items 中。

```Java
@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;
    // 内部错误码，非HTTP错误码
    private Integer code;
    private String message;
    private T data;

    public Result() {
    }

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> Result<T> success() {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), null);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(ErrorCode.SUCCESS.getCode(), ErrorCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        return new Result<>(errorCode.getCode(), errorCode.getMessage(), null);
    }

    public static <T> Result<T> fail(ErrorCode errorCode, String message) {
        return new Result<>(errorCode.getCode(), message, null);
    }

}
```

**程序错误码**

详细见 3\.6\.1 错误码

|code|含义|HTTP|
|---|---|---|
|0|成功|200|
|40001|参数校验失败|400|
|40100|未登录或令牌失效|401|
|40300|无操作权限|403|
|40400|资源不存在|404|
|40901|暂无可借库存|400|
|40902|借阅记录已归还，请勿重复操作|400|
|40903|用户名已存在|400|
|40904|资源已存在|400|
|50000|服务异常|500|

## 接口设计

统一前缀 /api，请求与响应均为 JSON；列表接口统一 page、pageSize（默认 20，上限 100），其余筛选参数随接口约定。下面先列接口清单，再给关键接口的请求与响应示例。

接口设计

|方法|路径|说明|权限|
|---|---|---|---|
|POST|/api/auth/register|注册，只创建 READER|公开|
|POST|/api/auth/login|登录，返回 token|公开|
|GET|/api/books|书籍列表，支持分类、关键词、仅看可借筛选与分页|公开|
|GET|/api/books/\{id\}|书籍详情与可借数量|公开|
|GET|/api/categories|启用状态的分类列表|公开|
|GET|/api/books/all|全部书籍（含下架），分页|ADMIN|
|POST|/api/books|新增书籍|ADMIN|
|PUT|/api/books/\{id\}|编辑书籍|ADMIN|
|PATCH|/api/books/\{id\}/enabled|上架 / 下架|ADMIN|
|GET|/api/categories/all|全部分类（含停用）|ADMIN|
|POST|/api/categories|新增分类|ADMIN|
|PUT|/api/categories/\{id\}|编辑分类|ADMIN|
|PATCH|/api/categories/\{id\}/enabled|启用 / 停用分类|ADMIN|
|GET|/api/users|用户列表，分页|ADMIN|
|PATCH|/api/users/\{id\}/status|启用 / 禁用用户|ADMIN|
|POST|/api/borrows|借阅，入参 bookId 与借期天数（7/15/30）|READER|
|GET|/api/borrows/my|我的借阅，按状态筛选并分页|READER|
|GET|/api/borrows/my/book-ids|当前用户未归还借阅涉及的书目 id|READER|
|GET|/api/borrows|全部借阅记录，支持按用户名筛选|ADMIN|
|POST|/api/borrows/\{id\}/return|办理归还|ADMIN|
|POST|/api/favorites|加入收藏|READER|
|DELETE|/api/favorites/\{bookId\}|取消收藏|READER|
|GET|/api/favorites|我的收藏列表，分页|READER|
|GET|/api/favorites/book-ids|当前用户收藏的书目 id|READER|
|GET|/api/notifications|通知列表，返回已读未读|登录|
|POST|/api/notifications/\{id\}/read|标记已读|登录|
|POST|/api/notifications/broadcast|发布全站公告|ADMIN|
|POST|/api/files/cover|上传书籍封面|ADMIN|

## 鉴权设计

### **登录与密码**

- 注册规则：注册只创建 READER；管理员账号由初始化脚本写入。

- 加密落库：密码用 bcrypt（cost 10）或 argon2id 加盐哈希后存 password 字段，禁止明文与可逆加密。

- 登录校验顺序：账号存在 → 密码匹配 → status=1；任一失败统一返回"用户名或密码错误"，不区分具体原因。

### **令牌**

采用无状态 JWT（HS256），载荷只放非敏感信息，不放用户名以外的个人信息。

```json
{ "sub": "1001", "role": "READER", "iat": 1762900000, "exp": 1762907200 }
```

- 有效期 2 小时；如需延长登录态，另发 7 天 refresh token，刷新时重新签发并校验用户 status。

- 客户端在请求头携带 Authorization: Bearer \<token\>。

- 退出登录由客户端丢弃令牌；

### **拦截器处理链**

- **接口权限控制**

**`RequireRole`**注解，修饰类或者HTTP接口，用于控制接口角色访问。具体限制见 3\.4接口设计

```Java
/**
 * 接口角色访问控制：修饰 Controller 类或方法，表示仅允许指定角色访问。
 * 方法级注解优先于类级注解；未标注则仅要求登录。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {

    Role[] value();
}
```

- **拦截器**

AuthInterceptor拦截所有HTTP请求，将token解析为JWT令牌，再解析出用户信息进行校验。接口要求角色与登录用户的角色匹配则放行，否则拦截报错403

## 异常处理

Controller层不捕获异常，异常直接抛给ControllerAdvice，包装成指定响应体

主动抛出业务异常返回400，有指定含义的场景返回对应的错误码，其他未捕获异常返回500

### 错误码

业务异常按作用层分为两类：`BusinessException`（应用层业务失败）与 `DomainException`（领域规则失败）；参数校验失败由 Spring 的 `MethodArgumentNotValidException` 统一处理，未识别异常兜底为 `SYSTEM_ERROR`。异常语义由错误码区分，调用方不手写数字。

```Java
public enum ErrorCode {

    SUCCESS(0, "ok", 200),
    PARAM_ERROR(40001, "参数校验失败", 400),
    UNAUTHORIZED(40100, "未登录或令牌失效", 401),
    FORBIDDEN(40300, "无操作权限", 403),
    NOT_FOUND(40400, "资源不存在", 404),
    NO_STOCK(40901, "暂无可借库存", 400),
    ALREADY_RETURNED(40902, "借阅记录已归还，请勿重复操作", 400),
    USERNAME_EXISTS(40903, "用户名已存在", 400),
    RESOURCE_EXISTS(40904, "资源已存在", 400),
    SYSTEM_ERROR(50000, "服务异常", 500);

    private final int code;

    private final String message;

    private final int httpStatus;

    ErrorCode(int code, String message, int httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

}
```

### 异常体

异常体基于错误码实现，异常信息可自定义，一般用于直接对用户展示。

异常信息一般格式为：简单描述错误原因 \+ 操作指引\(如有\)， 不得出现程序内部词汇

```Java
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() { return errorCode; }
}
```

`DomainException` 结构相同，用于领域层规则失败。

### 异常处理器

异常处理器负责将异常转换为统一的响应体，业务异常按错误码返回对应状态码与文案，未识别异常兜底为 500。

```Java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<Void>> handleBusiness(BusinessException e) {
        ErrorCode c = e.getErrorCode();
        return ResponseEntity.status(c.getHttpStatus()).body(Result.fail(c, e.getMessage()));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Result<Void>> handleDomain(DomainException e) {
        ErrorCode c = e.getErrorCode();
        return ResponseEntity.status(c.getHttpStatus()).body(Result.fail(c, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Result<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Result.fail(ErrorCode.PARAM_ERROR, msg));
    }

    /** 兜底：未识别异常一律 500，堆栈只进日志 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<Void>> handleUnknown(Exception e) {
        return ResponseEntity.status(ErrorCode.SYSTEM_ERROR.getHttpStatus())
                .body(Result.fail(ErrorCode.SYSTEM_ERROR));
    }
}
```



# 拓展功能\(待实现\)

1. 并发性拓展：目前库存修改依赖数据库行锁保证原子性，并发量上去之后可以采用 redis 预加载、预扣除库存，再通过 MQ 异步完成数据库更新。

2. 通知增强：当前只做站内信 + 进程内异步，后续可引入 MQ 实现更完整的通知分发、多通道（EMAIL）、投递重试与 event_key 幂等。

3. 个人资料管理：查看 / 修改昵称等资料。

4. 频繁登录锁定：暂未实现（需 Redis 计数）。
