package com.jiawa.train.batch.feign;

import com.jiawa.train.common.resp.CommonResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.Map;

// @FeignClient("business")
/** 远程调用 business 服务，生成每日车次。 */
@FeignClient(name = "business", url = "http://127.0.0.1:8002/business")
public interface BusinessFeign {

    @GetMapping("/hello")
    String hello();

    @GetMapping("/admin/daily-train/gen-daily/{date}")
    CommonResp<Object> genDaily(@PathVariable @DateTimeFormat(pattern = "yyyy-MM-dd") Date date);

    @GetMapping("/admin/confirm-order/cancel-timeout")
    CommonResp<Map<String, Object>> cancelTimeoutOrders(@RequestParam(required = false) Integer minutes);
}
