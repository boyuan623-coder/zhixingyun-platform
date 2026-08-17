package com.jiawa.train.common.interceptor;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.jiawa.train.common.util.JwtUtil;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.resp.MemberLoginResp;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 会员登录拦截器（common 模块）。 */
@Component
public class MemberInterceptor implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(MemberInterceptor.class);

    /**
     * 前置处理：解析 Header 中的 token，将会员信息绑定到当前线程。
     * 注意：此处不做 token 有效性拦截（网关已校验），仅负责解析与上下文写入。
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //获取header的token参数
        String token = request.getHeader("token");
        if (StrUtil.isNotBlank(token)) {
            JSONObject loginMember = JwtUtil.getJSONObject(token);
            LOG.info("当前登录会员 id={}", loginMember.get("id"));
            MemberLoginResp member = JSONUtil.toBean(loginMember, MemberLoginResp.class);
            LoginMemberContext.setMember(member);
        }
        return true;
    }

    /**
     * 请求完成后清理 ThreadLocal，防止线程池复用导致用户信息串号。
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        LoginMemberContext.remove();
    }

}
