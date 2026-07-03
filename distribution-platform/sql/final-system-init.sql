-- 企业级智能分销与订单履约平台初始化脚本
-- 执行方式：在 DataGrip 中连接本机 MySQL 后执行本文件。
-- 说明：脚本不会删除数据库，重复执行时通过唯一键更新演示数据。

SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

CREATE DATABASE IF NOT EXISTS user_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS product_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS stock_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS order_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS pay_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS auth_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE user_db;

CREATE TABLE IF NOT EXISTS sys_user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    status TINYINT DEFAULT 1,
    phone VARCHAR(20),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 密码均为 123456（BCrypt 加密），用于后续登录演示。
INSERT INTO sys_user (username, password, nickname, role, status, phone)
VALUES
    ('admin', '$2a$10$2vpREfFqoEbjuKj6p/1WuepbCnazpyr3Z1K1kDwejpF10fyMtzTqi', '系统管理员', 'admin', 1, '13800138000'),
    ('user01', '$2a$10$2vpREfFqoEbjuKj6p/1WuepbCnazpyr3Z1K1kDwejpF10fyMtzTqi', '普通用户01', 'user', 1, '13800138001'),
    ('user02', '$2a$10$2vpREfFqoEbjuKj6p/1WuepbCnazpyr3Z1K1kDwejpF10fyMtzTqi', '普通用户02', 'user', 1, '13800138002')
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    nickname = VALUES(nickname),
    role = VALUES(role),
    status = VALUES(status),
    phone = VALUES(phone);

USE auth_db;

CREATE TABLE IF NOT EXISTS auth_account (
    account_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    role VARCHAR(20) NOT NULL,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 密码均为 123456（BCrypt 加密），用于统一登录演示。
INSERT INTO auth_account (account_id, username, password, nickname, role, status)
VALUES
    (1, 'admin', '$2a$10$2vpREfFqoEbjuKj6p/1WuepbCnazpyr3Z1K1kDwejpF10fyMtzTqi', '系统管理员', 'admin', 1),
    (2, 'user01', '$2a$10$2vpREfFqoEbjuKj6p/1WuepbCnazpyr3Z1K1kDwejpF10fyMtzTqi', '普通用户01', 'user', 1),
    (3, 'user02', '$2a$10$2vpREfFqoEbjuKj6p/1WuepbCnazpyr3Z1K1kDwejpF10fyMtzTqi', '普通用户02', 'user', 1)
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    nickname = VALUES(nickname),
    role = VALUES(role),
    status = VALUES(status);

USE product_db;

CREATE TABLE IF NOT EXISTS product (
    product_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    price DECIMAL(10,2) NOT NULL,
    sku_code VARCHAR(50) UNIQUE,
    description VARCHAR(255),
    status TINYINT DEFAULT 1,
    safe_stock INT DEFAULT 20,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS browse_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    browse_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_browse_time (user_id, browse_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO product (product_id, product_name, category, price, sku_code, description, status, safe_stock)
VALUES
    (1, '机械键盘-Keychron K3', '外设', 368.00, 'SKU-K3-001', '超薄机械键盘，适合办公与开发场景', 1, 20),
    (2, '无线鼠标-罗技M720', '外设', 128.40, 'SKU-M720-002', '多设备无线鼠标，库存较低用于预警演示', 1, 20),
    (3, 'HDMI高清线-绿联2米', '线材', 35.00, 'SKU-HDMI-003', '2米高清 HDMI 连接线', 1, 20),
    (4, '铝合金笔记本支架', '办公', 89.00, 'SKU-STAND-004', '可折叠铝合金笔记本支架', 1, 20),
    (5, 'A4打印纸-得力70g', '办公耗材', 22.00, 'SKU-A4-005', '70g A4 打印纸', 1, 20),
    (6, 'USB-C扩展坞-倍思6合1', '电脑配件', 129.00, 'SKU-HUB-006', 'HDMI、USB3.0、PD 快充多接口扩展坞', 1, 20),
    (7, '电竞耳机-雷柏VH650', '外设', 149.00, 'SKU-HEADSET-007', '7.1 虚拟环绕声，降噪麦克风', 1, 20)
ON DUPLICATE KEY UPDATE
    product_name = VALUES(product_name),
    category = VALUES(category),
    price = VALUES(price),
    description = VALUES(description),
    status = VALUES(status),
    safe_stock = VALUES(safe_stock);

INSERT INTO browse_history (user_id, product_id, browse_time)
SELECT 1, p.product_id, NOW() - INTERVAL p.product_id HOUR
FROM product p
WHERE NOT EXISTS (
    SELECT 1 FROM browse_history h WHERE h.user_id = 1 AND h.product_id = p.product_id
);

INSERT INTO browse_history (user_id, product_id, browse_time)
SELECT demo.user_id, demo.product_id, demo.browse_time
FROM (
    SELECT 1 AS user_id, 1 AS product_id, '2025-05-18 09:20:30' AS browse_time UNION ALL
    SELECT 1, 2, '2025-05-18 10:15:20' UNION ALL
    SELECT 1, 4, '2025-05-18 14:30:10' UNION ALL
    SELECT 2, 3, '2025-05-17 11:05:40' UNION ALL
    SELECT 2, 5, '2025-05-17 15:20:15' UNION ALL
    SELECT 3, 1, '2025-05-19 08:40:25' UNION ALL
    SELECT 3, 7, '2025-05-19 09:10:35'
) demo
WHERE NOT EXISTS (
    SELECT 1
    FROM browse_history h
    WHERE h.user_id = demo.user_id
      AND h.product_id = demo.product_id
      AND h.browse_time = demo.browse_time
);

USE stock_db;

CREATE TABLE IF NOT EXISTS warehouse (
    warehouse_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    warehouse_name VARCHAR(50) NOT NULL,
    warehouse_address VARCHAR(200),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS stock (
    stock_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    product_id BIGINT NOT NULL,
    warehouse_id BIGINT NOT NULL,
    stock_num INT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_product_warehouse (product_id, warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO warehouse (warehouse_id, warehouse_name, warehouse_address, status)
VALUES
    (1, '北京顺义仓', '北京市顺义区空港工业区B区8号', 1),
    (2, '上海浦东仓', '上海市浦东新区金桥路1399号', 1),
    (3, '广州白云仓', '广州市白云区太和镇龙归东路10号', 1)
ON DUPLICATE KEY UPDATE
    warehouse_name = VALUES(warehouse_name),
    warehouse_address = VALUES(warehouse_address),
    status = VALUES(status);

INSERT INTO stock (product_id, warehouse_id, stock_num)
VALUES
    (1, 1, 50),
    (2, 1, 3),
    (3, 1, 100),
    (4, 1, 80),
    (5, 1, 200),
    (1, 2, 40),
    (2, 2, 0),
    (3, 2, 90),
    (4, 2, 70),
    (5, 2, 180),
    (6, 3, 60),
    (7, 3, 45)
ON DUPLICATE KEY UPDATE stock_num = VALUES(stock_num);

CREATE TABLE IF NOT EXISTS undo_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    branch_id BIGINT NOT NULL,
    xid VARCHAR(100) NOT NULL,
    context VARCHAR(128) NOT NULL,
    rollback_info LONGBLOB NOT NULL,
    log_status INT NOT NULL,
    log_created DATETIME NOT NULL,
    log_modified DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

USE order_db;

CREATE TABLE IF NOT EXISTS order_main (
    order_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    product_num INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    order_status VARCHAR(20) NOT NULL,
    receiver_name VARCHAR(50),
    receiver_phone VARCHAR(20),
    receiver_address VARCHAR(200),
    logistics_company VARCHAR(50),
    logistics_no VARCHAR(50),
    logistics_status VARCHAR(20),
    current_location VARCHAR(100),
    expect_arrive_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    pay_time DATETIME,
    delivery_time DATETIME,
    finish_time DATETIME,
    INDEX idx_user_id (user_id),
    INDEX idx_order_status (order_status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_item (
    item_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(100) NOT NULL,
    product_price DECIMAL(10,2) NOT NULL,
    quantity INT NOT NULL,
    sub_total DECIMAL(10,2) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_chat_record (
    chat_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    question TEXT NOT NULL,
    answer TEXT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS undo_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    branch_id BIGINT NOT NULL,
    xid VARCHAR(100) NOT NULL,
    context VARCHAR(128) NOT NULL,
    rollback_info LONGBLOB NOT NULL,
    log_status INT NOT NULL,
    log_created DATETIME NOT NULL,
    log_modified DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS seata_transaction_record (
    record_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no VARCHAR(64) NOT NULL,
    xid VARCHAR(128),
    transaction_status VARCHAR(32) NOT NULL,
    stock_branch_status VARCHAR(32) NOT NULL,
    pay_branch_status VARCHAR(32) NOT NULL,
    failure_reason VARCHAR(500),
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    INDEX idx_seata_record_order_no (order_no),
    INDEX idx_seata_record_xid (xid),
    INDEX idx_seata_record_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO order_main (
    order_no, user_id, product_name, product_num, total_amount, order_status,
    receiver_name, receiver_phone, receiver_address, logistics_company, logistics_no,
    logistics_status, current_location, expect_arrive_time, create_time, pay_time,
    delivery_time, finish_time
)
VALUES
    ('DD202505190001', 1, '机械键盘-Keychron K3', 2, 736.00, '已支付',
     '张三', '13800000000', '北京市朝阳区望京街道', '顺丰速运', 'SF100001',
     '待发货', '北京顺义仓', CONCAT(CURDATE(), ' 14:00:00'),
     CONCAT(CURDATE(), ' 09:30:00'), CONCAT(CURDATE(), ' 09:35:00'), NULL, NULL),
    ('DD202505190002', 1, '无线鼠标-罗技M720', 5, 642.00, '待支付',
     '李四', '13900000000', '上海市浦东新区世纪大道', NULL, NULL,
     NULL, NULL, NULL, CONCAT(CURDATE(), ' 09:45:00'), NULL, NULL, NULL),
    ('DD202505190003', 1, 'HDMI高清线-绿联2米', 10, 350.00, '已发货',
     '王五', '13700000000', '广州市天河区体育西路', '中通快递', 'ZTO100003',
     '运输中', '华东分拨中心', CONCAT(CURDATE(), ' 18:00:00'),
     CONCAT(CURDATE(), ' 10:00:00'), CONCAT(CURDATE(), ' 10:05:00'), CONCAT(CURDATE(), ' 11:30:00'), NULL),
    ('DD202505190004', 1, '铝合金笔记本支架', 3, 267.00, '库存不足',
     '赵六', '13600000000', '深圳市南山区科技园', NULL, NULL,
     NULL, NULL, NULL, CONCAT(CURDATE(), ' 10:15:00'), NULL, NULL, NULL),
    ('DD202505190005', 1, 'A4打印纸-得力70g', 20, 440.00, '已完成',
     '钱七', '13500000000', '杭州市西湖区文三路', '圆通快递', 'YTO100005',
     '已签收', '杭州市西湖区签收点', CONCAT(CURDATE(), ' 12:00:00'),
     CONCAT(CURDATE(), ' 08:30:00'), CONCAT(CURDATE(), ' 08:35:00'), CONCAT(CURDATE(), ' 09:10:00'), CONCAT(CURDATE(), ' 12:05:00')),
    ('DD202505190006', 3, 'USB-C扩展坞-倍思6合1', 1, 129.00, '已支付',
     '王五', '13800138002', '广州市天河区天河路385号', '申通快递', 'ST5566778899',
     '运输中', '广州市白云区中转中心', '2025-05-22 18:00:00',
     '2025-05-19 16:00:00', '2025-05-19 16:05:00', '2025-05-19 17:00:00', NULL),
    ('DD202505190007', 1, '电竞耳机-雷柏VH650', 1, 149.00, '待支付',
     '张三', '13800138000', '北京市海淀区中关村南大街5号', NULL, NULL,
     NULL, NULL, NULL, '2025-05-19 17:00:00', NULL, NULL, NULL)
ON DUPLICATE KEY UPDATE
    product_name = VALUES(product_name),
    product_num = VALUES(product_num),
    total_amount = VALUES(total_amount),
    order_status = VALUES(order_status),
    logistics_status = VALUES(logistics_status),
    current_location = VALUES(current_location),
    create_time = VALUES(create_time);

-- 生成 1275 条后台演示订单，配合上方 5 条可见订单后得到：
-- 今日订单 1280、今日成交额 128000、待发货 23、异常订单 5。
INSERT INTO order_main (
    order_no, user_id, product_name, product_num, total_amount, order_status,
    receiver_name, receiver_phone, receiver_address, logistics_company, logistics_no,
    logistics_status, current_location, expect_arrive_time, create_time, pay_time,
    delivery_time, finish_time
)
SELECT
    CONCAT('DD', DATE_FORMAT(CURDATE(), '%Y%m%d'), 'B', LPAD(seq.n, 4, '0')) AS order_no,
    1 AS user_id,
    '后台演示订单' AS product_name,
    1 AS product_num,
    CASE
        WHEN seq.n = 27 THEN 722.00
        WHEN seq.n BETWEEN 1 AND 22 THEN 100.00
        ELSE 99.00
    END AS total_amount,
    CASE
        WHEN seq.n BETWEEN 23 AND 26 THEN '库存不足'
        WHEN seq.n BETWEEN 1 AND 22 THEN '已支付'
        ELSE '已完成'
    END AS order_status,
    '演示用户' AS receiver_name,
    '13800000000' AS receiver_phone,
    '演示地址' AS receiver_address,
    CASE WHEN seq.n BETWEEN 23 AND 26 THEN NULL ELSE '系统物流' END AS logistics_company,
    CASE WHEN seq.n BETWEEN 23 AND 26 THEN NULL ELSE CONCAT('SYS', LPAD(seq.n, 6, '0')) END AS logistics_no,
    CASE
        WHEN seq.n BETWEEN 23 AND 26 THEN NULL
        WHEN seq.n BETWEEN 1 AND 22 THEN '待发货'
        ELSE '已签收'
    END AS logistics_status,
    CASE
        WHEN seq.n BETWEEN 23 AND 26 THEN NULL
        WHEN seq.n BETWEEN 1 AND 22 THEN '北京顺义仓'
        ELSE '签收完成'
    END AS current_location,
    CONCAT(CURDATE(), ' 23:00:00') AS expect_arrive_time,
    CONCAT(CURDATE(), ' 00:00:00') + INTERVAL seq.n SECOND AS create_time,
    CASE WHEN seq.n BETWEEN 23 AND 26 THEN NULL ELSE CONCAT(CURDATE(), ' 00:10:00') END AS pay_time,
    CASE WHEN seq.n >= 27 THEN CONCAT(CURDATE(), ' 01:00:00') ELSE NULL END AS delivery_time,
    CASE WHEN seq.n >= 27 THEN CONCAT(CURDATE(), ' 02:00:00') ELSE NULL END AS finish_time
FROM (
    SELECT d0.n + d1.n * 10 + d2.n * 100 + d3.n * 1000 + 1 AS n
    FROM
        (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d0
    CROSS JOIN
        (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d1
    CROSS JOIN
        (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4
         UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) d2
    CROSS JOIN
        (SELECT 0 n UNION ALL SELECT 1 UNION ALL SELECT 2) d3
) seq
WHERE seq.n BETWEEN 1 AND 1275
ON DUPLICATE KEY UPDATE
    total_amount = VALUES(total_amount),
    order_status = VALUES(order_status),
    logistics_status = VALUES(logistics_status),
    create_time = VALUES(create_time);

INSERT INTO order_item (order_id, product_id, product_name, product_price, quantity, sub_total, create_time)
SELECT o.order_id, p.product_id, o.product_name, p.price, o.product_num, o.total_amount, o.create_time
FROM order_main o
JOIN product_db.product p ON p.product_name = o.product_name
WHERE o.order_no IN ('DD202505190001', 'DD202505190002', 'DD202505190003', 'DD202505190004', 'DD202505190005',
                     'DD202505190006', 'DD202505190007')
  AND NOT EXISTS (SELECT 1 FROM order_item i WHERE i.order_id = o.order_id);

INSERT INTO ai_chat_record (user_id, question, answer, create_time)
SELECT demo.user_id, demo.question, demo.answer, demo.create_time
FROM (
    SELECT 1 AS user_id, '我的订单DD202505190001到哪了？' AS question,
           '订单 DD202505190001 当前位于北京顺义仓，预计今天 14:00 前送达。' AS answer,
           NOW() AS create_time UNION ALL
    SELECT 1, '推荐几款和机械键盘搭配的配件',
           '基于您浏览的机械键盘，为您推荐无线鼠标、笔记本支架和 USB-C 扩展坞。',
           '2025-05-18 10:00:00' UNION ALL
    SELECT 2, '库存不足的商品有哪些？',
           '当前库存低于安全阈值的商品有：无线鼠标-罗技M720，建议及时补货。',
           '2025-05-19 09:30:00' UNION ALL
    SELECT 3, '近7天哪些商品销量增长快？',
           '近7天销量增长较快的商品有机械键盘、USB-C 扩展坞和电竞耳机。',
           '2025-05-19 11:00:00' UNION ALL
    SELECT 2, '我的订单DD202505190005什么时候到？',
           '订单 DD202505190005 当前已签收，如有售后问题可继续联系在线客服。',
           '2025-05-20 10:00:00'
) demo
WHERE NOT EXISTS (SELECT 1 FROM ai_chat_record record WHERE record.question = demo.question);

USE pay_db;

CREATE TABLE IF NOT EXISTS payment (
    pay_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    pay_no VARCHAR(32) NOT NULL UNIQUE,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    pay_amount DECIMAL(10,2) NOT NULL,
    pay_method VARCHAR(20),
    pay_status VARCHAR(20) NOT NULL,
    callback_content TEXT,
    callback_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS undo_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    branch_id BIGINT NOT NULL,
    xid VARCHAR(100) NOT NULL,
    context VARCHAR(128) NOT NULL,
    rollback_info LONGBLOB NOT NULL,
    log_status INT NOT NULL,
    log_created DATETIME NOT NULL,
    log_modified DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO payment (pay_no, order_no, user_id, pay_amount, pay_method, pay_status, callback_content, callback_time)
VALUES
    ('PAY202505190001', 'DD202505190001', 1, 736.00, '支付宝', '支付成功', 'demo callback success', CONCAT(CURDATE(), ' 09:35:00')),
    ('PAY202505190004', 'DD202505190004', 1, 267.00, '微信支付', '支付失败', '{"reason":"库存不足"}', CONCAT(CURDATE(), ' 10:16:00')),
    ('PAY202505190003', 'DD202505190003', 1, 350.00, '微信支付', '支付成功', 'demo callback success', CONCAT(CURDATE(), ' 10:05:00')),
    ('PAY202505190005', 'DD202505190005', 1, 440.00, '支付宝', '支付成功', 'demo callback success', CONCAT(CURDATE(), ' 08:35:00')),
    ('PAY202505190006', 'DD202505190006', 3, 129.00, '微信支付', '支付成功', '{"out_trade_no":"PAY202505190006","trade_status":"SUCCESS"}', '2025-05-19 16:10:00'),
    ('PAY202505190007', 'DD202505190007', 1, 149.00, '支付宝', '待支付', NULL, NULL)
ON DUPLICATE KEY UPDATE
    pay_amount = VALUES(pay_amount),
    pay_method = VALUES(pay_method),
    pay_status = VALUES(pay_status),
    callback_content = VALUES(callback_content),
    callback_time = VALUES(callback_time);
