package com.jiawa.train.member.controller;

import com.jiawa.train.common.resp.CommonResp;
import com.jiawa.train.member.req.MemberLoginReq;
import com.jiawa.train.member.req.MemberRegisterReq;
import com.jiawa.train.member.req.MemberSendCodeReq;
import com.jiawa.train.member.resp.MemberLoginResp;
import com.jiawa.train.member.service.MemberService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** 会员业务 REST 控制器。 */
@RestController
@RequestMapping("/member")
public class MemberController {

    @Resource
    private MemberService memberService;

    /**
     * 统计会员总数（演示/测试用）。
     *
     * @return 会员记录条数
     */
    @GetMapping("/count")
    public CommonResp<Integer> count() {
        int count = memberService.count();
        CommonResp<Integer> commonResp = new CommonResp<>();
        commonResp.setContent(count);
        return commonResp;
    }

    /**
     * 显式注册会员（手机号唯一）。
     * 实际业务中更常用 send-code 的「静默注册」；本接口用于手机号已存在时抛业务异常的场景演示。
     *
     * @param req 注册请求，含手机号
     * @return 新会员主键 ID
     */
    @PostMapping("/register")
    public CommonResp<Long> register(@Valid MemberRegisterReq req) {
        long register = memberService.register(req);
        // CommonResp<Long> commonResp = new CommonResp<>();
        // commonResp.setContent(register);
        // return commonResp;
        return new CommonResp<>(register);
    }

    /**
     * 发送短信验证码（开发环境固定 8888，无需真实短信通道）。
     * 若手机号尚未注册，服务端会自动插入会员记录（静默注册）。
     *
     * @param req 发码请求，含手机号
     * @return 空业务体，仅表示调用成功
     */
    @PostMapping("/send-code")
    public CommonResp<Long> sendCode(@Valid @RequestBody MemberSendCodeReq req) {
        memberService.sendCode(req);
        return new CommonResp<>();
    }

    /**
     * 验证码登录，校验通过后签发 JWT 并返回给客户端。
     * 客户端后续请求在 Header 携带 token，由网关校验合法性，member 服务拦截器解析写入上下文。
     *
     * @param req 登录请求，含手机号与验证码
     * @return 会员信息 + JWT token
     */
    @PostMapping("/login")
    public CommonResp<MemberLoginResp> login(@Valid @RequestBody MemberLoginReq req) {
        MemberLoginResp resp = memberService.login(req);
        return new CommonResp<>(resp);
    }
}
