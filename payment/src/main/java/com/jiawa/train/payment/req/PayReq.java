package com.jiawa.train.payment.req;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PayReq {

    @NotNull(message = "【会员ID】不能为空")
    private Long memberId;

    @NotNull(message = "【订单ID】不能为空")
    private Long orderId;

    @NotNull(message = "【金额】不能为空")
    @DecimalMin(value = "0.01", message = "【金额】必须大于0")
    private BigDecimal amount;

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
