package com.jiawa.train.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
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
import com.jiawa.train.business.mapper.ConfirmOrderMapper;
import com.jiawa.train.business.mapper.DailyTrainSeatMapper;
import com.jiawa.train.business.mapper.DailyTrainTicketMapper;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderService.class);

    @Resource
    private ConfirmOrderMapper confirmOrderMapper;

    @Resource
    private DailyTrainTicketMapper dailyTrainTicketMapper;

    @Resource
    private DailyTrainSeatMapper dailyTrainSeatMapper;

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
        ConfirmOrderExample.Criteria criteria = confirmOrderExample.createCriteria();

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<ConfirmOrder> confirmOrderList = confirmOrderMapper.selectByExample(confirmOrderExample);

        PageInfo<ConfirmOrder> pageInfo = new PageInfo<>(confirmOrderList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<ConfirmOrderQueryResp> list = BeanUtil.copyToList(confirmOrderList, ConfirmOrderQueryResp.class);

        PageResp<ConfirmOrderQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        confirmOrderMapper.deleteByPrimaryKey(id);
    }

    @Transactional
    public void doConfirm(ConfirmOrderDoReq req) {
        req.setMemberId(getLoginMemberId());

        List<ConfirmOrderTicketReq> tickets = req.getTickets();
        if (CollUtil.isEmpty(tickets)) {
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_EMPTY);
        }
        if (tickets.size() > 5) {
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_TOO_MANY);
        }

        DailyTrainTicket dailyTrainTicket = dailyTrainTicketMapper.selectByPrimaryKey(req.getDailyTrainTicketId());
        if (ObjectUtil.isNull(dailyTrainTicket)) {
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_NOT_FOUND);
        }

        Map<String, Integer> seatTypeCountMap = buildSeatTypeCountMap(tickets);
        deductDailyTrainTicket(dailyTrainTicket, seatTypeCountMap);

        int startIndex = dailyTrainTicket.getStartIndex();
        int endIndex = dailyTrainTicket.getEndIndex();
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

        ConfirmOrder confirmOrder = new ConfirmOrder();
        confirmOrder.setId(SnowUtil.getSnowflakeNextId());
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
        LOG.info("购票成功，订单ID：{}", confirmOrder.getId());
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

    private List<DailyTrainSeat> pickSeatsInSameCarriage(Date date,
                                                         String trainCode,
                                                         List<ConfirmOrderTicketReq> tickets,
                                                         int startIndex,
                                                         int endIndex) {
        DailyTrainSeatExample example = new DailyTrainSeatExample();
        example.createCriteria()
                .andDateEqualTo(date)
                .andTrainCodeEqualTo(trainCode);
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

    private DailyTrainSeat findSeat(List<DailyTrainSeat> carriageSeats,
                                    ConfirmOrderTicketReq ticket,
                                    int startIndex,
                                    int endIndex,
                                    List<DailyTrainSeat> pickedSeats) {
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
