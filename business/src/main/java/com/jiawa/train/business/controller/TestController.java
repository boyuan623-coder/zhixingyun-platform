package com.jiawa.train.business.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 健康检查 / 联调测试接口。
 * <p>
 * <b>职责：</b>{@code GET /hello} 验证 business 服务存活；在 SpringMvcConfig 中排除登录拦截。
 */
@RestController
public class TestController {
    @GetMapping("/hello")
    public String hello() {
        return "Hello World! Business!";
    }
}
