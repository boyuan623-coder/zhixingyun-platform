package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.business.domain.ConfirmOrder;
import com.jiawa.train.business.domain.ConfirmOrderExample;
import com.jiawa.train.business.domain.DailyTrainSeat;
import com.jiawa.train.business.domain.DailyTrainSeatExample;
import com.jiawa.train.business.domain.DailyTrainTicket;
import com.jiawa.train.business.enums.ConfirmOrderStatusEnum;
import com.jiawa.train.business.enums.SeatTypeEnum;
import com.jiawa.train.business.feign.PaymentFeign;
import com.jiawa.train.business.mapper.ConfirmOrderMapper;
import com.jiawa.train.business.mapper.DailyTrainSeatMapper;
import com.jiawa.train.business.mapper.DailyTrainTicketMapper;
import com.jiawa.train.business.mq.ConfirmOrderAsyncDispatcher;
import com.jiawa.train.business.mq.ConfirmOrderMessage;
import com.jiawa.train.business.redis.TicketQueryCacheService;
import com.jiawa.train.business.redis.TicketStockRedisService;
import com.jiawa.train.business.req.ConfirmOrderDoReq;
import com.jiawa.train.business.req.ConfirmOrderQueryReq;
import com.jiawa.train.business.req.ConfirmOrderTicketReq;
import com.jiawa.train.business.resp.ConfirmOrderQueryResp;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 购票确认服务（Redis 锁 + Lua 库存 + MQ 削峰 + Seata 支付）。
 */
@Service
public class ConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;
    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;
    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private TicketStockRedisService ticketStockRedisService;
    @Resource
    private TicketQueryCacheService ticketQueryCacheService;
    @Resource
    private ConfirmOrderAsyncDispatcher confirmOrderAsyncDispatcher;
    @Resource
    private PaymentFeign paymentFeign;
    @Resource
    private ConfirmOrderTxService confirmOrderTxService;

    /** true=接口只入队；false=同步走完整购票（压测超卖用） */
    @Value("${train.confirm.async:true}")
    private boolean asyncConfirm;

    public void save(ConfirmOrderDoReq req) {
        DateTime now = DateTime.now();
        ConfirmOrder confirmOrder = BeanUtil.copyProperties(req, ConfirmOrder.class);
        if (ObjectUtil.isNull(confirmOrder.getId())) {
            confirmOrder.setId(SnowUtil.getSnowflakeNextId());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.insert(confirmOrder);
        } else {
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.updateByPrimaryKey(confirmOrder);
        }
    }

    public PageResp<ConfirmOrderQueryResp> queryList(ConfirmOrderQueryReq req) {
        ConfirmOrderExample confirmOrderExample = new ConfirmOrderExample();
        confirmOrderExample.setOrderByClause("id desc");
        PageHelper.startPage(req.getPage(), req.getSize());
        List<ConfirmOrder> confirmOrderList = confirmOrderMapper.selectByExample(confirmOrderExample);
        PageInfo<ConfirmOrder> pageInfo = new PageInfo<>(confirmOrderList);
        List<ConfirmOrderQueryResp> list = BeanUtil.copyToList(confirmOrderList, ConfirmOrderQueryResp.class);
        PageResp<ConfirmOrderQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        confirmOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 用户下单入口：默认 MQ 异步削峰；可通过配置或 /do-sync 同步执行。
     */
    public Long doConfirm(ConfirmOrderDoReq req) {
        req.setMemberId(getLoginMemberId());
        validateTickets(req.getTickets());
        if (asyncConfirm) {
            return submitAsync(req);
        }
        return doConfirmSync(req);
    }

    /** 同步购票（JMeter 超卖压测入口） */
    public Long doConfirmSync(ConfirmOrderDoReq req) {
        if (req.getMemberId() == null) {
            req.setMemberId(getLoginMemberId());
        }
        validateTickets(req.getTickets());
        Long orderId = SnowUtil.getSnowflakeNextId();
        processConfirm(orderId, req);
        return orderId;
    }

    /** 快速受理：落 INIT 订单 + 发 MQ */
    public Long submitAsync(ConfirmOrderDoReq req) {
        DateTime now = DateTime.now();
        Long orderId = SnowUtil.getSnowflakeNextId();
        ConfirmOrder order = new ConfirmOrder();
        order.setId(orderId);
        order.setMemberId(req.getMemberId());
        order.setDate(req.getDate());
        order.setTrainCode(req.getTrainCode());
        order.setStart(req.getStart());
        order.setEnd(req.getEnd());
        order.setDailyTrainTicketId(req.getDailyTrainTicketId());
        order.setTickets(JSONUtil.toJsonStr(req.getTickets()));
        order.setStatus(ConfirmOrderStatusEnum.INIT.getCode());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        confirmOrderMapper.insert(order);

        confirmOrderAsyncDispatcher.dispatch(new ConfirmOrderMessage(orderId, req));
        LOG.info("抢票请求已入队 orderId={}", orderId);
        return orderId;
    }

    /**
     * 核心处理：Redisson 锁（看门狗续期）→ Lua 扣 Redis 库存 → Seata 全局事务（DB库存/座位/订单/支付）。
     */
    public void processConfirm(Long orderId, ConfirmOrderDoReq req) {
        Long ticketId = req.getDailyTrainTicketId();
        RLock lock = redissonClient.getLock("lock:confirm:" + ticketId);
        boolean locked = false;
        boolean redisDeducted = false;
        Map<String, Integer> seatTypeCountMap = null;
        try {
            // 不传 leaseTime → 启用看门狗自动续期
            locked = lock.tryLock(5, TimeUnit.SECONDS);
            if (!locked) {
                markFailure(orderId);
                throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_FAIL);
            }

            DailyTrainTicket dailyTrainTicket = dailyTrainTicketMapper.selectByPrimaryKey(ticketId);
            if (ObjectUtil.isNull(dailyTrainTicket)) {
                markFailure(orderId);
                throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_NOT_FOUND);
            }

            seatTypeCountMap = buildSeatTypeCountMap(req.getTickets());
            // 若 Redis 无库存键则从 DB 预热后再扣
            ensureRedisStock(dailyTrainTicket);
            redisDeducted = ticketStockRedisService.deduct(ticketId, seatTypeCountMap);
            if (!redisDeducted) {
                markFailure(orderId);
                throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_NOT_ENOUGH);
            }

            try {
                confirmOrderTxService.doConfirmInGlobalTx(orderId, req, dailyTrainTicket, seatTypeCountMap);
            } catch (RuntimeException ex) {
                ticketStockRedisService.restore(ticketId, seatTypeCountMap);
                redisDeducted = false;
                markFailure(orderId);
                throw ex;
            }

            ticketQueryCacheService.evictByTicket(
                    dailyTrainTicket.getDate(), dailyTrainTicket.getTrainCode(),
                    dailyTrainTicket.getStart(), dailyTrainTicket.getEnd());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            markFailure(orderId);
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_FAIL);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /** 订单超时取消：回滚座位/DB余票/Redis库存，并退款 */
    @Transactional
    public int cancelTimeoutOrders(int timeoutMinutes) {
        Date deadline = DateTime.now().offset(DateField.MINUTE, -timeoutMinutes);
        ConfirmOrderExample example = new ConfirmOrderExample();
        example.createCriteria()
                .andStatusEqualTo(ConfirmOrderStatusEnum.INIT.getCode())
                .andCreateTimeLessThan(deadline);
        List<ConfirmOrder> list = confirmOrderMapper.selectByExampleWithBLOBs(example);
        int count = 0;
        for (ConfirmOrder order : list) {
            cancelOne(order, false);
            count++;
        }

        ConfirmOrderExample pendingEx = new ConfirmOrderExample();
        pendingEx.createCriteria()
                .andStatusEqualTo(ConfirmOrderStatusEnum.PENDING.getCode())
                .andCreateTimeLessThan(deadline);
        List<ConfirmOrder> pendingList = confirmOrderMapper.selectByExampleWithBLOBs(pendingEx);
        for (ConfirmOrder order : pendingList) {
            cancelOne(order, true);
            count++;
        }
        return count;
    }

    private void cancelOne(ConfirmOrder order, boolean restoreStock) {
        DateTime now = DateTime.now();
        if (restoreStock && StrUtil.isNotBlank(order.getTickets())) {
            List<ConfirmOrderTicketReq> tickets = JSONUtil.toList(order.getTickets(), ConfirmOrderTicketReq.class);
            Map<String, Integer> map = buildSeatTypeCountMap(tickets);
            DailyTrainTicket ticket = dailyTrainTicketMapper.selectByPrimaryKey(order.getDailyTrainTicketId());
            if (ticket != null) {
                // 回滚 DB 余票
                restoreDailyTrainTicket(ticket, map);
                ticketStockRedisService.restore(ticket.getId(), map);
                // 回滚座位位图
                restoreSeats(order, ticket, tickets);
                BigDecimal amount = calcAmount(ticket, map);
                try {
                    paymentFeign.refund(new PaymentFeign.PayFeignReq(order.getMemberId(), order.getId(), amount));
                } catch (Exception e) {
                    LOG.warn("退款调用失败 orderId={}", order.getId(), e);
                }
                ticketQueryCacheService.evictByTicket(ticket.getDate(), ticket.getTrainCode(), ticket.getStart(), ticket.getEnd());
            }
        }
        ConfirmOrder update = new ConfirmOrder();
        update.setId(order.getId());
        update.setStatus(ConfirmOrderStatusEnum.CANCEL.getCode());
        update.setUpdateTime(now);
        confirmOrderMapper.updateByPrimaryKeySelective(update);
        LOG.info("超时取消订单 orderId={}", order.getId());
    }

    private void restoreSeats(ConfirmOrder order, DailyTrainTicket ticket, List<ConfirmOrderTicketReq> tickets) {
        int startIndex = ticket.getStartIndex();
        int endIndex = ticket.getEndIndex();
        DailyTrainSeatExample example = new DailyTrainSeatExample();
        example.createCriteria().andDateEqualTo(order.getDate()).andTrainCodeEqualTo(order.getTrainCode());
        List<DailyTrainSeat> all = dailyTrainSeatMapper.selectByExample(example);
        DateTime now = DateTime.now();
        for (ConfirmOrderTicketReq t : tickets) {
            if (StrUtil.isBlank(t.getSeat())) {
                continue;
            }
            String col = t.getSeat().substring(0, 1);
            String row = normalizeRow(t.getSeat().substring(1));
            for (DailyTrainSeat seat : all) {
                if (ObjectUtil.equal(seat.getCol(), col) && ObjectUtil.equal(seat.getRow(), row)
                        && ObjectUtil.equal(seat.getSeatType(), t.getSeatTypeCode())) {
                    String newSell = unmarkSell(seat.getSell(), startIndex, endIndex);
                    DailyTrainSeat upd = new DailyTrainSeat();
                    upd.setId(seat.getId());
                    upd.setSell(newSell);
                    upd.setUpdateTime(now);
                    dailyTrainSeatMapper.updateByPrimaryKeySelective(upd);
                    break;
                }
            }
        }
    }

    private void ensureRedisStock(DailyTrainTicket ticket) {
        Integer edz = ticketStockRedisService.getStock(ticket.getId(), SeatTypeEnum.EDZ.getCode());
        if (edz == null) {
            ticketStockRedisService.warmStock(ticket);
        }
    }

    private void markFailure(Long orderId) {
        if (orderId == null) {
            return;
        }
        ConfirmOrder existing = confirmOrderMapper.selectByPrimaryKey(orderId);
        if (existing == null) {
            return;
        }
        ConfirmOrder update = new ConfirmOrder();
        update.setId(orderId);
        update.setStatus(ConfirmOrderStatusEnum.FAILURE.getCode());
        update.setUpdateTime(DateTime.now());
        confirmOrderMapper.updateByPrimaryKeySelective(update);
    }

    private void validateTickets(List<ConfirmOrderTicketReq> tickets) {
        if (CollUtil.isEmpty(tickets)) {
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_EMPTY);
        }
        if (tickets.size() > 5) {
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_TOO_MANY);
        }
    }

    private Long getLoginMemberId() {
        try {
            return LoginMemberContext.getId();
        } catch (Exception e) {
            throw new BusinessException(BusinessExceptionEnum.MEMBER_NOT_LOGIN);
        }
    }

    private Map<String, Integer> buildSeatTypeCountMap(List<ConfirmOrderTicketReq> tickets) {
        Map<String, Integer> seatTypeCountMap = new HashMap<>();
        for (ConfirmOrderTicketReq ticket : tickets) {
            seatTypeCountMap.merge(ticket.getSeatTypeCode(), 1, Integer::sum);
        }
        return seatTypeCountMap;
    }

    private BigDecimal calcAmount(DailyTrainTicket ticket, Map<String, Integer> seatTypeCountMap) {
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> e : seatTypeCountMap.entrySet()) {
            SeatTypeEnum type = SeatTypeEnum.getEnumByCode(e.getKey());
            if (type == null) {
                continue;
            }
            BigDecimal price = switch (type) {
                case YDZ -> ticket.getYdzPrice();
                case EDZ -> ticket.getEdzPrice();
                case RW -> ticket.getRwPrice();
                case YW -> ticket.getYwPrice();
            };
            if (price == null) {
                price = BigDecimal.ONE;
            }
            total = total.add(price.multiply(BigDecimal.valueOf(e.getValue())));
        }
        return total;
    }

    private void restoreDailyTrainTicket(DailyTrainTicket ticket, Map<String, Integer> map) {
        DailyTrainTicket update = new DailyTrainTicket();
        update.setId(ticket.getId());
        update.setUpdateTime(DateTime.now());
        for (Map.Entry<String, Integer> e : map.entrySet()) {
            SeatTypeEnum type = SeatTypeEnum.getEnumByCode(e.getKey());
            int c = e.getValue();
            switch (type) {
                case YDZ -> update.setYdz(ticket.getYdz() + c);
                case EDZ -> update.setEdz(ticket.getEdz() + c);
                case RW -> update.setRw(ticket.getRw() + c);
                case YW -> update.setYw(ticket.getYw() + c);
                default -> {
                }
            }
        }
        dailyTrainTicketMapper.updateByPrimaryKeySelective(update);
    }

    private String unmarkSell(String sell, int startIndex, int endIndex) {
        char[] chars = sell.toCharArray();
        for (int i = startIndex - 1; i <= endIndex - 2; i++) {
            chars[i] = '0';
        }
        return new String(chars);
    }

    private String normalizeRow(String row) {
        return StrUtil.fillBefore(row, '0', 2);
    }
}
