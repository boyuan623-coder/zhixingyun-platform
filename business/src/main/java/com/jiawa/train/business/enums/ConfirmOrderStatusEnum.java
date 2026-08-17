package com.jiawa.train.business.enums;

/** 确认订单状态枚举。 */
public enum ConfirmOrderStatusEnum {

    /** 初始，订单已创建尚未处理 */
    INIT("I", "初始"),
    /** 处理中，如异步抢票、支付中 */
    PENDING("P", "处理中"),
    /** 购票成功，座位与余票已扣减 */
    SUCCESS("S", "成功"),
    /** 处理失败 */
    FAILURE("F", "失败"),
    /** 无票（库存不足） */
    EMPTY("E", "无票"),
    /** 用户或系统取消 */
    CANCEL("C", "取消");

    private String code;

    private String desc;

    ConfirmOrderStatusEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override    public String toString() {
        return "ConfirmOrderStatusEnum{" +
                "code='" + code + '\'' +
                ", desc='" + desc + '\'' +
                "} " + super.toString();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

}
