package com.jiawa.train.business.controller.admin;

import com.jiawa.train.business.service.ConfirmOrderService;
import com.jiawa.train.common.resp.CommonResp;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理端：手动触发订单超时取消（也可由 batch Quartz 调用）。
 */
@RestController
@RequestMapping("/admin/confirm-order")
public class ConfirmOrderAdminController {

    @Resource
    private ConfirmOrderService confirmOrderService;

    @Value("${train.order.timeout-minutes:15}")
    private int timeoutMinutes;

    @GetMapping("/cancel-timeout")
    public CommonResp<Map<String, Object>> cancelTimeout(
            @RequestParam(required = false) Integer minutes) {
        int m = minutes == null ? timeoutMinutes : minutes;
        int n = confirmOrderService.cancelTimeoutOrders(m);
        Map<String, Object> data = new HashMap<>();
        data.put("cancelled", n);
        data.put("timeoutMinutes", m);
        return new CommonResp<>(data);
    }
}
