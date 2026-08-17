package com.jiawa.train.business.mapper;

import com.jiawa.train.business.domain.DailyTrainTicket;
import com.jiawa.train.business.domain.DailyTrainTicketExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * daily_train_ticket 表数据访问层，每日区间余票的数据库操作。
 */

public interface DailyTrainTicketMapper {
    long countByExample(DailyTrainTicketExample example);

    int deleteByExample(DailyTrainTicketExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DailyTrainTicket record);

    int insertSelective(DailyTrainTicket record);

    List<DailyTrainTicket> selectByExample(DailyTrainTicketExample example);

    DailyTrainTicket selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DailyTrainTicket record, @Param("example") DailyTrainTicketExample example);

    int updateByExample(@Param("record") DailyTrainTicket record, @Param("example") DailyTrainTicketExample example);

    int updateByPrimaryKeySelective(DailyTrainTicket record);

    int updateByPrimaryKey(DailyTrainTicket record);
}