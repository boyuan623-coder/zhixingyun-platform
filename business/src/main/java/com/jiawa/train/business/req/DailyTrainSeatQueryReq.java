package com.jiawa.train.business.req;

import com.jiawa.train.common.req.PageReq;

/**
 * 每日座位分页查询请求。
 */

public class DailyTrainSeatQueryReq extends PageReq {

    private String trainCode;

    public String getTrainCode() {
        return trainCode;
    }

    public void setTrainCode(String trainCode) {
        this.trainCode = trainCode;
    }

    @Override
    public String toString() {
        return "DailyTrainSeatQueryReq{" +
                "trainCode='" + trainCode + '\'' +
                "} " + super.toString();
    }
}
