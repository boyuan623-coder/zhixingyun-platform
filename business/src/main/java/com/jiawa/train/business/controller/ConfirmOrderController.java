package com.jiawa.train.business.controller;

import com.jiawa.train.business.req.ConfirmOrderDoReq;
import com.jiawa.train.business.service.ConfirmOrderService;
import com.jiawa.train.common.resp.CommonResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户端购票确认。
 * <ul>
 *   <li>{@code /do} 默认 MQ 异步削峰</li>
 *   <li>{@code /do-sync} 同步完整链路（锁+Lua+Seata），用于超卖压测</li>
 * </ul>
 */
@RestController
@RequestMapping("/confirm-order")
public class ConfirmOrderController {

    @Resource
    private ConfirmOrderService confirmOrderService;

    @PostMapping("/do")
    public CommonResp<Map<String, Object>> doConfirm(@Valid @RequestBody ConfirmOrderDoReq req) {
        Long orderId = confirmOrderService.doConfirm(req);
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("async", true);
        return new CommonResp<>(data);
    }

    @PostMapping("/do-sync")
    public CommonResp<Map<String, Object>> doConfirmSync(@Valid @RequestBody ConfirmOrderDoReq req) {
        Long orderId = confirmOrderService.doConfirmSync(req);
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("async", false);
        return new CommonResp<>(data);
    }
}
