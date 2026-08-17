package com.jiawa.train.business.mapper;

import com.jiawa.train.business.domain.TrainCarriage;
import com.jiawa.train.business.domain.TrainCarriageExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

/**
 * train_carriage 表数据访问层，车厢模板的数据库操作。
 */

public interface TrainCarriageMapper {
    long countByExample(TrainCarriageExample example);

    int deleteByExample(TrainCarriageExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TrainCarriage record);

    int insertSelective(TrainCarriage record);

    List<TrainCarriage> selectByExample(TrainCarriageExample example);

    TrainCarriage selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TrainCarriage record, @Param("example") TrainCarriageExample example);

    int updateByExample(@Param("record") TrainCarriage record, @Param("example") TrainCarriageExample example);

    int updateByPrimaryKeySelective(TrainCarriage record);

    int updateByPrimaryKey(TrainCarriage record);
}