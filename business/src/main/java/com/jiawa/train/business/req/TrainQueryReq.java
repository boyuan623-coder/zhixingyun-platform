package com.jiawa.train.business.req;

import com.jiawa.train.common.req.PageReq;

/**
 * 车次模板分页查询请求，支持按条件筛选车次基础配置。
 */

public class TrainQueryReq extends PageReq {

    @Override
    public String toString() {
        return "TrainQueryReq{" +
                "} " + super.toString();
    }
}
