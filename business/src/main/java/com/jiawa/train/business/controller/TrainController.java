package com.jiawa.train.business.controller;

import com.jiawa.train.business.resp.TrainQueryResp;
import com.jiawa.train.business.service.TrainService;
import com.jiawa.train.common.resp.CommonResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端 —— 车次列表接口。
 * <p>
 * <b>职责：</b>返回全部基础车次，供前端筛选或展示。
 */
@RestController
@RequestMapping("/train")
public class TrainController {

    @Resource
    private TrainService trainService;

    @GetMapping("/query-all")
    public CommonResp<List<TrainQueryResp>> queryList() {
        List<TrainQueryResp> list = trainService.queryAll();
        return new CommonResp<>(list);
    }

}
