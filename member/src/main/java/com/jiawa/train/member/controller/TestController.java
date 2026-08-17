package com.jiawa.train.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 健康检查 / 冒烟测试控制器。 */
@RestController
public class TestController {

    /**
     * 返回固定字符串，用于快速验证 HTTP 通路。
     *
     * @return 固定响应 "Hello World!"
     */
    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }
}
