package com.jiawa.train.business.redis;

import cn.hutool.core.date.DateUtil;
import com.jiawa.train.business.resp.DailyTrainTicketQueryResp;
import com.jiawa.train.common.resp.PageResp;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * 余票查询缓存：开售前预热后，查询优先读 Redis。
 */
@Service
public class TicketQueryCacheService {

    private static final String QUERY_KEY_PREFIX = "ticket:query:";

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public String buildKey(Date date, String trainCode, String start, String end, Integer page, Integer size) {
        String d = date == null ? "-" : DateUtil.formatDate(date);
        return QUERY_KEY_PREFIX + d + ":"
                + nullToDash(trainCode) + ":"
                + nullToDash(start) + ":"
                + nullToDash(end) + ":"
                + page + ":" + size;
    }

    @SuppressWarnings("unchecked")
    public PageResp<DailyTrainTicketQueryResp> get(String key) {
        Object val = redisTemplate.opsForValue().get(key);
        if (val instanceof PageResp) {
            return (PageResp<DailyTrainTicketQueryResp>) val;
        }
        return null;
    }

    public void put(String key, PageResp<DailyTrainTicketQueryResp> pageResp) {
        // 余票缓存短 TTL，扣票后主动失效；预热场景可覆盖
        redisTemplate.opsForValue().set(key, pageResp, 10, TimeUnit.MINUTES);
    }

    public void putWarm(String key, PageResp<DailyTrainTicketQueryResp> pageResp) {
        redisTemplate.opsForValue().set(key, pageResp, 2, TimeUnit.HOURS);
    }

    public void evictByTicket(Date date, String trainCode, String start, String end) {
        // 简化：按常见分页尺寸清理
        for (int page = 1; page <= 5; page++) {
            for (int size : new int[]{10, 20, 50, 100}) {
                redisTemplate.delete(buildKey(date, trainCode, start, end, page, size));
                redisTemplate.delete(buildKey(date, trainCode, null, null, page, size));
                redisTemplate.delete(buildKey(date, null, start, end, page, size));
                redisTemplate.delete(buildKey(date, null, null, null, page, size));
            }
        }
    }

    private String nullToDash(String s) {
        return s == null || s.isBlank() ? "-" : s;
    }
}
