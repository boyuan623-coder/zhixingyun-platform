package com.jiawa.train.gateway.config;

import com.jiawa.train.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/** 会员登录全局过滤器（gateway 模块）——第一层鉴权。 */
@Component
public class LoginMemberFilter implements Ordered, GlobalFilter {

    private static final Logger LOG = LoggerFactory.getLogger(LoginMemberFilter.class);

    /**
     * 全局过滤：白名单放行，非白名单路径校验 token 有效性。
     *
     * @param exchange 当前请求/响应上下文
     * @param chain    过滤器链，校验通过后调用 {@code chain.filter} 转发下游
     * @return 响应式 Mono
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 白名单：管理端、健康检查、登录/发验证码接口无需 JWT
        if (path.contains("/admin")
                || path.contains("/hello")
                || path.contains("/member/member/login")
                || path.contains("/member/member/send-code")
                || path.contains("/daily-train-ticket/warm-up")
                || path.contains("/daily-train-ticket/query-list-db")
                || path.contains("/payment/pay/")) {
            LOG.info("不需要登录验证：{}", path);
            return chain.filter(exchange);
        } else {
            LOG.info("需要登录验证：{}", path);
        }
        // 获取header的token参数
        String token = exchange.getRequest().getHeaders().getFirst("token");
        LOG.info("会员登录验证开始");
        if (token == null || token.isEmpty()) {
            LOG.info( "token为空，请求被拦截" );
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // 校验 token 签名与过期时间（网关只做有效性判断，不解析用户身份）
        boolean validate = JwtUtil.validate(token);
        if (validate) {
            LOG.info("token有效，放行该请求");
            return chain.filter(exchange);
        } else {
            LOG.warn( "token无效，请求被拦截" );
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

    }

    /**
     * 过滤器优先级，值越小越先执行。设为 0 确保鉴权在其他业务 Filter 之前。
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
