package com.jiawa.train.business.mq;

import com.jiawa.train.business.req.ConfirmOrderDoReq;

import java.io.Serializable;

/**
 * 异步抢票消息体。
 */
public class ConfirmOrderMessage implements Serializable {

    private Long orderId;
    private ConfirmOrderDoReq req;

    public ConfirmOrderMessage() {
    }

    public ConfirmOrderMessage(Long orderId, ConfirmOrderDoReq req) {
        this.orderId = orderId;
        this.req = req;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public ConfirmOrderDoReq getReq() {
        return req;
    }

    public void setReq(ConfirmOrderDoReq req) {
        this.req = req;
    }
}
