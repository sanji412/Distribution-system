-- Seata AT 模式回滚日志表初始化脚本
-- 执行位置：DataGrip 连接本机 MySQL 后执行本文件。
-- 说明：order_db、stock_db、pay_db 参与下单全局事务，必须包含 undo_log 表。

SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

CREATE DATABASE IF NOT EXISTS order_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS stock_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS pay_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS order_db.undo_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    branch_id BIGINT NOT NULL COMMENT '分支事务ID',
    xid VARCHAR(128) NOT NULL COMMENT '全局事务ID',
    context VARCHAR(128) NOT NULL COMMENT '上下文信息',
    rollback_info LONGBLOB NOT NULL COMMENT '回滚快照数据',
    log_status INT NOT NULL COMMENT '日志状态',
    log_created DATETIME NOT NULL COMMENT '创建时间',
    log_modified DATETIME NOT NULL COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT undo log';

CREATE TABLE IF NOT EXISTS stock_db.undo_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    branch_id BIGINT NOT NULL COMMENT '分支事务ID',
    xid VARCHAR(128) NOT NULL COMMENT '全局事务ID',
    context VARCHAR(128) NOT NULL COMMENT '上下文信息',
    rollback_info LONGBLOB NOT NULL COMMENT '回滚快照数据',
    log_status INT NOT NULL COMMENT '日志状态',
    log_created DATETIME NOT NULL COMMENT '创建时间',
    log_modified DATETIME NOT NULL COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT undo log';

CREATE TABLE IF NOT EXISTS pay_db.undo_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    branch_id BIGINT NOT NULL COMMENT '分支事务ID',
    xid VARCHAR(128) NOT NULL COMMENT '全局事务ID',
    context VARCHAR(128) NOT NULL COMMENT '上下文信息',
    rollback_info LONGBLOB NOT NULL COMMENT '回滚快照数据',
    log_status INT NOT NULL COMMENT '日志状态',
    log_created DATETIME NOT NULL COMMENT '创建时间',
    log_modified DATETIME NOT NULL COMMENT '修改时间',
    PRIMARY KEY (id),
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata AT undo log';

CREATE TABLE IF NOT EXISTS order_db.seata_transaction_record (
    record_id BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    order_no VARCHAR(64) NOT NULL COMMENT '订单编号',
    xid VARCHAR(128) DEFAULT NULL COMMENT '全局事务ID',
    transaction_status VARCHAR(32) NOT NULL COMMENT '全局事务状态：RUNNING/COMMITTED/ROLLED_BACK',
    stock_branch_status VARCHAR(32) NOT NULL COMMENT '库存分支状态',
    pay_branch_status VARCHAR(32) NOT NULL COMMENT '支付分支状态',
    failure_reason VARCHAR(500) DEFAULT NULL COMMENT '失败原因',
    create_time DATETIME NOT NULL COMMENT '创建时间',
    update_time DATETIME NOT NULL COMMENT '更新时间',
    PRIMARY KEY (record_id),
    KEY idx_seata_record_order_no (order_no),
    KEY idx_seata_record_xid (xid),
    KEY idx_seata_record_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata 全局事务审计记录';

USE order_db;

DROP PROCEDURE IF EXISTS add_seata_undo_unique_index;

DELIMITER //
CREATE PROCEDURE add_seata_undo_unique_index(IN db_name VARCHAR(64))
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM information_schema.statistics
        WHERE table_schema = db_name
          AND table_name = 'undo_log'
          AND index_name = 'ux_undo_log'
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE ', db_name, '.undo_log ADD UNIQUE KEY ux_undo_log (xid, branch_id)');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END//
DELIMITER ;

CALL add_seata_undo_unique_index('order_db');
CALL add_seata_undo_unique_index('stock_db');
CALL add_seata_undo_unique_index('pay_db');

DROP PROCEDURE IF EXISTS add_seata_undo_unique_index;
