-- 预置数据（幂等，可重复执行）
-- 注意：id=1 固定为「其他」分类，停用分类下的图书在展示层归并到该分类（数据不修改）

-- 分类：id=1 保留给「其他」
INSERT IGNORE INTO categories (id, name, enabled) VALUES
  (1, '其他', 1),
  (2, '计算机', 1),
  (3, '文学', 1),
  (4, '历史', 1);

-- 示例书目（available_count = total_count，副本全部可借）
INSERT IGNORE INTO books
  (id, isbn, name, author, publisher, publish_year, description, cover_url, category_id, total_count, available_count, enabled)
VALUES
  (1, '9787111128069', '算法导论', 'Cormen', '机械工业出版社', 2006, '算法领域经典教材', NULL, 2, 3, 3, 1),
  (2, '9787111135234', '深入理解计算机系统', 'Bryant', '机械工业出版社', 2016, '计算机系统经典', NULL, 2, 2, 2, 1),
  (3, '9787020002207', '红楼梦', '曹雪芹', '人民文学出版社', 1996, '中国古典四大名著之一', NULL, 3, 2, 2, 1),
  (4, '9787508647357', '万历十五年', '黄仁宇', '生活·读书·新知三联书店', 1997, '明史研究经典', NULL, 4, 1, 1, 1);

-- 图书副本（每本 total_count 册，全部可借）
INSERT IGNORE INTO book_copy (id, book_id, barcode, status) VALUES
  (1, 1, 'CPY-0001-01', 'AVAILABLE'),
  (2, 1, 'CPY-0001-02', 'AVAILABLE'),
  (3, 1, 'CPY-0001-03', 'AVAILABLE'),
  (4, 2, 'CPY-0002-01', 'AVAILABLE'),
  (5, 2, 'CPY-0002-02', 'AVAILABLE'),
  (6, 3, 'CPY-0003-01', 'AVAILABLE'),
  (7, 3, 'CPY-0003-02', 'AVAILABLE'),
  (8, 4, 'CPY-0004-01', 'AVAILABLE');

-- 预置管理员账号（admin / admin123456）
INSERT IGNORE INTO users (id, user_name, password, role, status) VALUES
  (1, 'admin', '$2a$10$2J94wa9Sy5mnM4XrJ3en9OCzeI9GiiQ3bMylOdMxiY.IV3y8hBVFG', 'ADMIN', 1);
