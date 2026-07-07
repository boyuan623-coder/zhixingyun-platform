package com.jiawa.train.common.exception;

public enum BusinessExceptionEnum {

    MEMBER_MOBILE_EXIST("手机号已注册"),
    MEMBER_MOBILE_NOT_EXIST("请先获取短信验证码"),
    MEMBER_MOBILE_CODE_ERROR("短信验证码错误"),

    BUSINESS_STATION_NAME_UNIQUE_ERROR("车站已存在"),
    BUSINESS_TRAIN_CODE_UNIQUE_ERROR("车次编号已存在"),
    BUSINESS_TRAIN_STATION_INDEX_UNIQUE_ERROR("同车次站序已存在"),
    BUSINESS_TRAIN_STATION_NAME_UNIQUE_ERROR("同车次站名已存在"),
    BUSINESS_TRAIN_CARRIAGE_INDEX_UNIQUE_ERROR("同车次厢号已存在"),

    MEMBER_NOT_LOGIN("未登录或登录超时"),
    CONFIRM_ORDER_TICKET_NOT_FOUND("余票记录不存在"),
    CONFIRM_ORDER_TICKET_NOT_ENOUGH("余票不足"),
    CONFIRM_ORDER_SEAT_NOT_ENOUGH("座位不足"),
    CONFIRM_ORDER_TICKET_EMPTY("请选择乘车人"),
    CONFIRM_ORDER_TICKET_TOO_MANY("最多只能购买5张车票");

    private String desc;

    BusinessExceptionEnum(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "BusinessExceptionEnum{" +
                "desc='" + desc + '\'' +
                "} " + super.toString();
    }
}
