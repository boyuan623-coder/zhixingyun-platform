USE train_business;

-- 若已存在 amount 列可忽略报错
ALTER TABLE confirm_order
  ADD COLUMN amount DECIMAL(12,2) NULL COMMENT '订单金额' AFTER tickets;
