-- 支付库：账户 + 流水（Seata AT 需配合 undo_log）
CREATE DATABASE IF NOT EXISTS train_payment DEFAULT CHARACTER SET utf8mb4;
USE train_payment;

DROP TABLE IF EXISTS pay_account;
CREATE TABLE pay_account (
  id BIGINT NOT NULL PRIMARY KEY,
  member_id BIGINT NOT NULL COMMENT '会员ID',
  balance DECIMAL(12,2) NOT NULL DEFAULT 0 COMMENT '余额',
  create_time DATETIME(3),
  update_time DATETIME(3),
  UNIQUE KEY uk_member (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员支付账户';

DROP TABLE IF EXISTS pay_record;
CREATE TABLE pay_record (
  id BIGINT NOT NULL PRIMARY KEY,
  member_id BIGINT NOT NULL,
  order_id BIGINT NOT NULL,
  amount DECIMAL(12,2) NOT NULL,
  type CHAR(1) NOT NULL COMMENT 'D扣款 R退款',
  create_time DATETIME(3),
  UNIQUE KEY uk_order_type (order_id, type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付流水';

-- Seata AT undo
CREATE TABLE IF NOT EXISTS undo_log (
  branch_id BIGINT NOT NULL,
  xid VARCHAR(128) NOT NULL,
  context VARCHAR(128) NOT NULL,
  rollback_info LONGBLOB NOT NULL,
  log_status INT NOT NULL,
  log_created DATETIME(6) NOT NULL,
  log_modified DATETIME(6) NOT NULL,
  UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 演示账户：大余额，便于压测
INSERT INTO pay_account (id, member_id, balance, create_time, update_time)
VALUES (1, 1, 999999.00, NOW(3), NOW(3))
ON DUPLICATE KEY UPDATE balance = 999999.00;
