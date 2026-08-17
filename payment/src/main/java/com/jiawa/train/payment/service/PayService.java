package com.jiawa.train.payment.service;

import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.payment.domain.PayAccount;
import com.jiawa.train.payment.domain.PayRecord;
import com.jiawa.train.payment.mapper.PayAccountMapper;
import com.jiawa.train.payment.mapper.PayRecordMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class PayService {

    private static final Logger LOG = LoggerFactory.getLogger(PayService.class);

    private static final String TYPE_DEDUCT = "D";
    private static final String TYPE_REFUND = "R";
    private static final BigDecimal DEMO_INIT_BALANCE = new BigDecimal("999999");

    @Resource
    private PayAccountMapper payAccountMapper;

    @Resource
    private PayRecordMapper payRecordMapper;

    @Transactional(rollbackFor = Exception.class)
    public void deduct(Long memberId, Long orderId, BigDecimal amount) {
        PayAccount account = payAccountMapper.selectByMemberId(memberId);
        if (account == null) {
            account = createDemoAccount(memberId);
        }
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException(BusinessExceptionEnum.PAY_BALANCE_NOT_ENOUGH);
        }
        BigDecimal newBalance = account.getBalance().subtract(amount);
        payAccountMapper.updateBalance(memberId, newBalance);
        insertRecord(memberId, orderId, amount, TYPE_DEDUCT);
        LOG.info("扣款成功 memberId={}, orderId={}, amount={}", memberId, orderId, amount);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refund(Long memberId, Long orderId, BigDecimal amount) {
        PayRecord exist = payRecordMapper.selectByOrderIdAndType(orderId, TYPE_REFUND);
        if (exist != null) {
            LOG.info("退款流水已存在，跳过 orderId={}", orderId);
            return;
        }
        PayAccount account = payAccountMapper.selectByMemberId(memberId);
        if (account == null) {
            throw new BusinessException(BusinessExceptionEnum.PAY_ACCOUNT_ERROR);
        }
        BigDecimal newBalance = account.getBalance().add(amount);
        payAccountMapper.updateBalance(memberId, newBalance);
        insertRecord(memberId, orderId, amount, TYPE_REFUND);
        LOG.info("退款成功 memberId={}, orderId={}, amount={}", memberId, orderId, amount);
    }

    private PayAccount createDemoAccount(Long memberId) {
        Date now = new Date();
        PayAccount account = new PayAccount();
        account.setId(SnowUtil.getSnowflakeNextId());
        account.setMemberId(memberId);
        account.setBalance(DEMO_INIT_BALANCE);
        account.setCreateTime(now);
        account.setUpdateTime(now);
        payAccountMapper.insert(account);
        return account;
    }

    private void insertRecord(Long memberId, Long orderId, BigDecimal amount, String type) {
        PayRecord record = new PayRecord();
        record.setId(SnowUtil.getSnowflakeNextId());
        record.setMemberId(memberId);
        record.setOrderId(orderId);
        record.setAmount(amount);
        record.setType(type);
        record.setCreateTime(new Date());
        payRecordMapper.insert(record);
    }
}
