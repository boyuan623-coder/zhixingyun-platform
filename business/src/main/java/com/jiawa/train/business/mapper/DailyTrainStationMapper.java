package com.jiawa.train.business.mapper;

import com.jiawa.train.business.domain.DailyTrainStation;
import com.jiawa.train.business.domain.DailyTrainStationExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * daily_train_station 表数据访问层，每日途经站的数据库操作。
 */

public interface DailyTrainStationMapper {
    long countByExample(DailyTrainStationExample example);

    int deleteByExample(DailyTrainStationExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DailyTrainStation record);

    int insertSelective(DailyTrainStation record);

    List<DailyTrainStation> selectByExample(DailyTrainStationExample example);

    DailyTrainStation selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DailyTrainStation record, @Param("example") DailyTrainStationExample example);

    int updateByExample(@Param("record") DailyTrainStation record, @Param("example") DailyTrainStationExample example);

    int updateByPrimaryKeySelective(DailyTrainStation record);

    int updateByPrimaryKey(DailyTrainStation record);
}