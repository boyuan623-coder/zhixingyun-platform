package com.jiawa.train.business.feign;

import com.jiawa.train.common.resp.CommonResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "payment", url = "http://127.0.0.1:8004/payment")
public interface PaymentFeign {

    @PostMapping("/pay/deduct")
    CommonResp<Object> deduct(@RequestBody PayFeignReq req);

    @PostMapping("/pay/refund")
    CommonResp<Object> refund(@RequestBody PayFeignReq req);

    class PayFeignReq {
        private Long memberId;
        private Long orderId;
        private BigDecimal amount;

        public PayFeignReq() {
        }

        public PayFeignReq(Long memberId, Long orderId, BigDecimal amount) {
            this.memberId = memberId;
            this.orderId = orderId;
            this.amount = amount;
        }

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
}
