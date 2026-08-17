package com.jiawa.train.business.controller;

import com.jiawa.train.business.req.DailyTrainTicketQueryReq;
import com.jiawa.train.business.resp.DailyTrainTicketQueryResp;
import com.jiawa.train.business.service.DailyTrainTicketService;
import com.jiawa.train.common.resp.CommonResp;
import com.jiawa.train.common.resp.PageResp;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户端 —— 余票查询（含 Redis 缓存）与预热/直查对比接口。
 */
@RestController
@RequestMapping("/daily-train-ticket")
public class DailyTrainTicketController {

    @Resource
    private DailyTrainTicketService dailyTrainTicketService;

    @GetMapping("/query-list")
    public CommonResp<PageResp<DailyTrainTicketQueryResp>> queryList(@Valid DailyTrainTicketQueryReq req) {
        PageResp<DailyTrainTicketQueryResp> list = dailyTrainTicketService.queryList(req);
        return new CommonResp<>(list);
    }

    /** 强制查库，压测对比缓存用 */
    @GetMapping("/query-list-db")
    public CommonResp<PageResp<DailyTrainTicketQueryResp>> queryListDb(@Valid DailyTrainTicketQueryReq req) {
        return new CommonResp<>(dailyTrainTicketService.queryListFromDb(req));
    }

    /** 开售前缓存预热 */
    @GetMapping("/warm-up")
    public CommonResp<Map<String, Object>> warmUp(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date date) {
        int n = dailyTrainTicketService.warmUp(date);
        Map<String, Object> data = new HashMap<>();
        data.put("warmedTickets", n);
        return new CommonResp<>(data);
    }
}
