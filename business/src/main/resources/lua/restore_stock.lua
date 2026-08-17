-- 回滚库存
for i = 1, #KEYS do
  redis.call('INCRBY', KEYS[i], ARGV[i])
end
return 1
