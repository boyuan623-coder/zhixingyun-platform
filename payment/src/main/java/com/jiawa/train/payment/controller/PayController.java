package com.jiawa.train.payment.controller;

import com.jiawa.train.common.resp.CommonResp;
import com.jiawa.train.payment.req.PayReq;
import com.jiawa.train.payment.service.PayService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pay")
public class PayController {

    @Resource
    private PayService payService;

    @PostMapping("/deduct")
    public CommonResp<Object> deduct(@Valid @RequestBody PayReq req) {
        payService.deduct(req.getMemberId(), req.getOrderId(), req.getAmount());
        return new CommonResp<>();
    }

    @PostMapping("/refund")
    public CommonResp<Object> refund(@Valid @RequestBody PayReq req) {
        payService.refund(req.getMemberId(), req.getOrderId(), req.getAmount());
        return new CommonResp<>();
    }
}
