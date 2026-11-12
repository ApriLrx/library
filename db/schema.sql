-- 图书管理系统建表脚本（MySQL 8.0 / InnoDB / utf8mb4）
-- 只建逻辑外键，不建物理外键约束（互联网惯例，见 readme 设计评审）
-- 按外键依赖顺序执行：categories → books → book_copy → users → borrow_record → favorite → notification

-- 1. 图书分类
CREATE TABLE IF NOT EXISTS categories (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  name        VARCHAR(100)    NOT NULL COMMENT '分类名称',
  enabled     TINYINT         NOT NULL DEFAULT 1 COMMENT '1 启用 / 0 停用',
  created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_categories_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图书分类';

-- 2. 书目
CREATE TABLE IF NOT EXISTS books (
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
CREATE TABLE IF NOT EXISTS book_copy (
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
CREATE TABLE IF NOT EXISTS users (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_name   VARCHAR(50)     NOT NULL COMMENT '用户名',
  password    VARCHAR(100)    NOT NULL COMMENT '密码哈希，bcrypt/argon2',
  role        VARCHAR(20)     NOT NULL DEFAULT 'READER' COMMENT 'READER/ADMIN/SUPER_ADMIN',
  status      TINYINT         NOT NULL DEFAULT 1 COMMENT '1 正常 / 0 禁用',
  created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_users_name (user_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户';

-- 5. 借阅记录
CREATE TABLE IF NOT EXISTS borrow_record (
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
CREATE TABLE IF NOT EXISTS favorite (
  id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id     BIGINT UNSIGNED NOT NULL,
  book_id     BIGINT UNSIGNED NOT NULL,
  created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_fav_user_book (user_id, book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏';

-- 7. 通知
CREATE TABLE IF NOT EXISTS notification (
  id               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  user_id          BIGINT UNSIGNED     NULL COMMENT '接收人，NULL 为全站公告',
  book_id          BIGINT UNSIGNED     NULL COMMENT '相关书目',
  borrow_record_id BIGINT UNSIGNED     NULL COMMENT '关联借阅记录',
  type             VARCHAR(20)     NOT NULL COMMENT 'DUE_REMIND/OVERDUE/AVAILABLE/SYSTEM',
  content          VARCHAR(1000)   NOT NULL COMMENT '通知内容',
  read_at          DATETIME            NULL COMMENT '已读时间',
  created_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  KEY idx_notify_user_read (user_id, read_at),
  KEY idx_notify_borrow_type (borrow_record_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知';
