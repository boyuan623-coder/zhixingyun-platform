package com.jiawa.train.payment.mapper;

import com.jiawa.train.payment.domain.PayRecord;
import org.apache.ibatis.annotations.Param;

public interface PayRecordMapper {

    int insert(PayRecord record);

    PayRecord selectByOrderIdAndType(@Param("orderId") Long orderId, @Param("type") String type);
}
