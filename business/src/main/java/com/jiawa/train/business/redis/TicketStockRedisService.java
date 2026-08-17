package com.jiawa.train.business.redis;

import com.jiawa.train.business.domain.DailyTrainTicket;
import com.jiawa.train.business.enums.SeatTypeEnum;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Redis 余票库存：预热 + Lua 原子扣减/回滚。
 */
@Service
public class TicketStockRedisService {

    private static final Logger LOG = LoggerFactory.getLogger(TicketStockRedisService.class);

    public static final String STOCK_KEY_PREFIX = "ticket:stock:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final DefaultRedisScript<Long> deductScript;
    private final DefaultRedisScript<Long> restoreScript;

    public TicketStockRedisService() {
        deductScript = new DefaultRedisScript<>();
        deductScript.setResultType(Long.class);
        deductScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/deduct_stock.lua")));

        restoreScript = new DefaultRedisScript<>();
        restoreScript.setResultType(Long.class);
        restoreScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/restore_stock.lua")));
    }

    public String stockKey(Long ticketId, String seatTypeCode) {
        return STOCK_KEY_PREFIX + ticketId + ":" + seatTypeCode;
    }

    /** 将单条 O-D 余票写入 Redis 库存键 */
    public void warmStock(DailyTrainTicket ticket) {
        if (ticket == null || ticket.getId() == null) {
            return;
        }
        stringRedisTemplate.opsForValue().set(stockKey(ticket.getId(), SeatTypeEnum.YDZ.getCode()),
                String.valueOf(nullToZero(ticket.getYdz())));
        stringRedisTemplate.opsForValue().set(stockKey(ticket.getId(), SeatTypeEnum.EDZ.getCode()),
                String.valueOf(nullToZero(ticket.getEdz())));
        stringRedisTemplate.opsForValue().set(stockKey(ticket.getId(), SeatTypeEnum.RW.getCode()),
                String.valueOf(nullToZero(ticket.getRw())));
        stringRedisTemplate.opsForValue().set(stockKey(ticket.getId(), SeatTypeEnum.YW.getCode()),
                String.valueOf(nullToZero(ticket.getYw())));
    }

    public void warmStockBatch(List<DailyTrainTicket> tickets) {
        if (tickets == null) {
            return;
        }
        for (DailyTrainTicket ticket : tickets) {
            warmStock(ticket);
        }
        LOG.info("余票库存预热完成，条数={}", tickets.size());
    }

    /**
     * Lua 原子扣减。成功返回 true；任一席别不足返回 false（脚本内不部分扣减）。
     */
    public boolean deduct(Long ticketId, Map<String, Integer> seatTypeCountMap) {
        List<String> keys = new ArrayList<>();
        List<String> args = new ArrayList<>();
        for (Map.Entry<String, Integer> e : seatTypeCountMap.entrySet()) {
            keys.add(stockKey(ticketId, e.getKey()));
            args.add(String.valueOf(e.getValue()));
        }
        Long result = stringRedisTemplate.execute(deductScript, keys, args.toArray());
        return result != null && result == 1L;
    }

    public void restore(Long ticketId, Map<String, Integer> seatTypeCountMap) {
        List<String> keys = new ArrayList<>();
        List<String> args = new ArrayList<>();
        for (Map.Entry<String, Integer> e : seatTypeCountMap.entrySet()) {
            keys.add(stockKey(ticketId, e.getKey()));
            args.add(String.valueOf(e.getValue()));
        }
        stringRedisTemplate.execute(restoreScript, keys, args.toArray());
    }

    public Integer getStock(Long ticketId, String seatTypeCode) {
        String v = stringRedisTemplate.opsForValue().get(stockKey(ticketId, seatTypeCode));
        if (v == null) {
            return null;
        }
        return Integer.parseInt(v);
    }

    private int nullToZero(Integer v) {
        return v == null ? 0 : v;
    }
}
