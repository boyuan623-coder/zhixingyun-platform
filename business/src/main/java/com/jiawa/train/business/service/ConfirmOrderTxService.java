package com.jiawa.train.business.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.jiawa.train.business.domain.ConfirmOrder;
import com.jiawa.train.business.domain.DailyTrainSeat;
import com.jiawa.train.business.domain.DailyTrainSeatExample;
import com.jiawa.train.business.domain.DailyTrainTicket;
import com.jiawa.train.business.enums.ConfirmOrderStatusEnum;
import com.jiawa.train.business.enums.SeatTypeEnum;
import com.jiawa.train.business.feign.PaymentFeign;
import com.jiawa.train.business.mapper.ConfirmOrderMapper;
import com.jiawa.train.business.mapper.DailyTrainSeatMapper;
import com.jiawa.train.business.mapper.DailyTrainTicketMapper;
import com.jiawa.train.business.req.ConfirmOrderDoReq;
import com.jiawa.train.business.req.ConfirmOrderTicketReq;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.common.resp.CommonResp;
import io.seata.spring.annotation.GlobalTransactional;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Seata 全局事务边界（独立 Bean，避免同类自调用导致注解失效）。
 */
@Service
public class ConfirmOrderTxService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderTxService.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;
    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;
    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;
    @Resource
    private PaymentFeign paymentFeign;

    /**
     * Seata 开启时走全局事务；关闭时退化为本地 @Transactional + Feign 支付调用。
     */
    @GlobalTransactional(name = "confirm-order-tx", rollbackFor = Exception.class, timeoutMills = 60000)
    @Transactional(rollbackFor = Exception.class)
    public void doConfirmInGlobalTx(Long orderId,
                                    ConfirmOrderDoReq req,
                                    DailyTrainTicket dailyTrainTicket,
                                    Map<String, Integer> seatTypeCountMap) {
        deductDailyTrainTicket(dailyTrainTicket, seatTypeCountMap);

        int startIndex = dailyTrainTicket.getStartIndex();
        int endIndex = dailyTrainTicket.getEndIndex();
        List<ConfirmOrderTicketReq> tickets = req.getTickets();
        List<DailyTrainSeat> pickedSeats = pickSeatsInSameCarriage(
                req.getDate(), req.getTrainCode(), tickets, startIndex, endIndex);
        if (CollUtil.isEmpty(pickedSeats)) {
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_SEAT_NOT_ENOUGH);
        }

        DateTime now = DateTime.now();
        for (int i = 0; i < tickets.size(); i++) {
            ConfirmOrderTicketReq ticket = tickets.get(i);
            DailyTrainSeat dailyTrainSeat = pickedSeats.get(i);
            String newSell = markSell(dailyTrainSeat.getSell(), startIndex, endIndex);
            DailyTrainSeat seatUpdate = new DailyTrainSeat();
            seatUpdate.setId(dailyTrainSeat.getId());
            seatUpdate.setSell(newSell);
            seatUpdate.setUpdateTime(now);
            dailyTrainSeatMapper.updateByPrimaryKeySelective(seatUpdate);
            ticket.setSeat(dailyTrainSeat.getCol() + trimRowLeadingZero(dailyTrainSeat.getRow()));
        }

        BigDecimal amount = calcAmount(dailyTrainTicket, seatTypeCountMap);
        ConfirmOrder existing = confirmOrderMapper.selectByPrimaryKey(orderId);
        if (existing == null) {
            ConfirmOrder confirmOrder = new ConfirmOrder();
            confirmOrder.setId(orderId);
            confirmOrder.setMemberId(req.getMemberId());
            confirmOrder.setDate(req.getDate());
            confirmOrder.setTrainCode(req.getTrainCode());
            confirmOrder.setStart(req.getStart());
            confirmOrder.setEnd(req.getEnd());
            confirmOrder.setDailyTrainTicketId(req.getDailyTrainTicketId());
            confirmOrder.setTickets(JSONUtil.toJsonStr(tickets));
            confirmOrder.setStatus(ConfirmOrderStatusEnum.SUCCESS.getCode());
            confirmOrder.setCreateTime(now);
            confirmOrder.setUpdateTime(now);
            confirmOrderMapper.insert(confirmOrder);
        } else {
            ConfirmOrder update = new ConfirmOrder();
            update.setId(orderId);
            update.setTickets(JSONUtil.toJsonStr(tickets));
            update.setStatus(ConfirmOrderStatusEnum.SUCCESS.getCode());
            update.setUpdateTime(now);
            confirmOrderMapper.updateByPrimaryKeySelective(update);
        }

        CommonResp<Object> payResp = paymentFeign.deduct(
                new PaymentFeign.PayFeignReq(req.getMemberId(), orderId, amount));
        if (payResp == null || !payResp.getSuccess()) {
            throw new BusinessException(BusinessExceptionEnum.PAY_BALANCE_NOT_ENOUGH);
        }
        LOG.info("购票+支付成功 orderId={} amount={}", orderId, amount);
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

    private void deductDailyTrainTicket(DailyTrainTicket dailyTrainTicket, Map<String, Integer> seatTypeCountMap) {
        DailyTrainTicket update = new DailyTrainTicket();
        update.setId(dailyTrainTicket.getId());
        update.setUpdateTime(DateTime.now());
        for (Map.Entry<String, Integer> entry : seatTypeCountMap.entrySet()) {
            SeatTypeEnum seatTypeEnum = SeatTypeEnum.getEnumByCode(entry.getKey());
            if (ObjectUtil.isNull(seatTypeEnum)) {
                throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_NOT_ENOUGH);
            }
            int count = entry.getValue();
            switch (seatTypeEnum) {
                case YDZ -> {
                    if (dailyTrainTicket.getYdz() < count) {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_NOT_ENOUGH);
                    }
                    update.setYdz(dailyTrainTicket.getYdz() - count);
                    dailyTrainTicket.setYdz(dailyTrainTicket.getYdz() - count);
                }
                case EDZ -> {
                    if (dailyTrainTicket.getEdz() < count) {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_NOT_ENOUGH);
                    }
                    update.setEdz(dailyTrainTicket.getEdz() - count);
                    dailyTrainTicket.setEdz(dailyTrainTicket.getEdz() - count);
                }
                case RW -> {
                    if (dailyTrainTicket.getRw() < count) {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_NOT_ENOUGH);
                    }
                    update.setRw(dailyTrainTicket.getRw() - count);
                    dailyTrainTicket.setRw(dailyTrainTicket.getRw() - count);
                }
                case YW -> {
                    if (dailyTrainTicket.getYw() < count) {
                        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_NOT_ENOUGH);
                    }
                    update.setYw(dailyTrainTicket.getYw() - count);
                    dailyTrainTicket.setYw(dailyTrainTicket.getYw() - count);
                }
                default -> throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_NOT_ENOUGH);
            }
        }
        dailyTrainTicketMapper.updateByPrimaryKeySelective(update);
    }

    private List<DailyTrainSeat> pickSeatsInSameCarriage(Date date, String trainCode,
                                                        List<ConfirmOrderTicketReq> tickets,
                                                        int startIndex, int endIndex) {
        DailyTrainSeatExample example = new DailyTrainSeatExample();
        example.createCriteria().andDateEqualTo(date).andTrainCodeEqualTo(trainCode);
        List<DailyTrainSeat> allSeats = dailyTrainSeatMapper.selectByExample(example);
        if (CollUtil.isEmpty(allSeats)) {
            return null;
        }
        Set<Integer> carriageIndexes = allSeats.stream()
                .map(DailyTrainSeat::getCarriageIndex)
                .collect(Collectors.toCollection(HashSet::new));
        for (Integer carriageIndex : carriageIndexes) {
            List<DailyTrainSeat> carriageSeats = allSeats.stream()
                    .filter(item -> ObjectUtil.equal(item.getCarriageIndex(), carriageIndex))
                    .collect(Collectors.toList());
            List<DailyTrainSeat> pickedSeats = new ArrayList<>();
            boolean success = true;
            for (ConfirmOrderTicketReq ticket : tickets) {
                DailyTrainSeat seat = findSeat(carriageSeats, ticket, startIndex, endIndex, pickedSeats);
                if (ObjectUtil.isNull(seat)) {
                    success = false;
                    break;
                }
                pickedSeats.add(seat);
            }
            if (success) {
                return pickedSeats;
            }
        }
        return null;
    }

    private DailyTrainSeat findSeat(List<DailyTrainSeat> carriageSeats, ConfirmOrderTicketReq ticket,
                                    int startIndex, int endIndex, List<DailyTrainSeat> pickedSeats) {
        String expectedCol = null;
        String expectedRow = null;
        if (StrUtil.isNotBlank(ticket.getSeat())) {
            expectedCol = ticket.getSeat().substring(0, 1);
            expectedRow = normalizeRow(ticket.getSeat().substring(1));
        }
        for (DailyTrainSeat seat : carriageSeats) {
            if (!ObjectUtil.equal(seat.getSeatType(), ticket.getSeatTypeCode())) {
                continue;
            }
            if (pickedSeats.stream().anyMatch(item -> ObjectUtil.equal(item.getId(), seat.getId()))) {
                continue;
            }
            if (StrUtil.isNotBlank(expectedCol)
                    && (!ObjectUtil.equal(seat.getCol(), expectedCol)
                    || !ObjectUtil.equal(seat.getRow(), expectedRow))) {
                continue;
            }
            if (canSell(seat.getSell(), startIndex, endIndex)) {
                return seat;
            }
        }
        return null;
    }

    private boolean canSell(String sell, int startIndex, int endIndex) {
        if (StrUtil.isBlank(sell)) {
            return false;
        }
        for (int i = startIndex - 1; i <= endIndex - 2; i++) {
            if (sell.charAt(i) != '0') {
                return false;
            }
        }
        return true;
    }

    private String markSell(String sell, int startIndex, int endIndex) {
        char[] chars = sell.toCharArray();
        for (int i = startIndex - 1; i <= endIndex - 2; i++) {
            chars[i] = '1';
        }
        return new String(chars);
    }

    private String normalizeRow(String row) {
        return StrUtil.fillBefore(row, '0', 2);
    }

    private String trimRowLeadingZero(String row) {
        return String.valueOf(Integer.parseInt(row));
    }
}
