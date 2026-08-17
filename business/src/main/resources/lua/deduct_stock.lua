-- 原子扣减多席别库存：先全部校验，再全部扣减，避免部分成功
-- KEYS[i] = 库存 key；ARGV[i] = 扣减数量
for i = 1, #KEYS do
  local current = tonumber(redis.call('GET', KEYS[i]) or '0')
  local need = tonumber(ARGV[i])
  if current < need then
    return 0
  end
end
for i = 1, #KEYS do
  redis.call('DECRBY', KEYS[i], ARGV[i])
end
return 1
