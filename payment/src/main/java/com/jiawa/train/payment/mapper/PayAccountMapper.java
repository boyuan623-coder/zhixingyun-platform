package com.jiawa.train.payment.mapper;

import com.jiawa.train.payment.domain.PayAccount;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

public interface PayAccountMapper {

    PayAccount selectByMemberId(Long memberId);

    int updateBalance(@Param("memberId") Long memberId, @Param("balance") BigDecimal balance);

    int insert(PayAccount record);
}
