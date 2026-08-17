package com.jiawa.train.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

/**
 * 网关令牌桶限流（Redis + Lua），默认限制抢票接口。
 */
@Component
public class TokenBucketRateLimitFilter implements GlobalFilter, Ordered {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final DefaultRedisScript<Long> script;

    @Value("${train.gateway.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${train.gateway.rate-limit.capacity:50}")
    private long capacity;

    @Value("${train.gateway.rate-limit.rate:50}")
    private long rate;

    public TokenBucketRateLimitFilter(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.script = new DefaultRedisScript<>();
        this.script.setLocation(new ClassPathResource("lua/token_bucket.lua"));
        this.script.setResultType(Long.class);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (!enabled) {
            return chain.filter(exchange);
        }
        String path = exchange.getRequest().getURI().getPath();
        if (!path.contains("/confirm-order/do")) {
            return chain.filter(exchange);
        }
        String key = "gateway:tb:confirm-order";
        long now = System.currentTimeMillis();
        List<String> keys = Collections.singletonList(key);
        List<String> args = List.of(
                String.valueOf(capacity),
                String.valueOf(rate),
                String.valueOf(now));
        return redisTemplate.execute(script, keys, args)
                .single(0L)
                .flatMap(allowed -> {
                    if (allowed != null && allowed == 1L) {
                        return chain.filter(exchange);
                    }
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    byte[] body = "{\"success\":false,\"message\":\"请求过于频繁，请稍后重试\"}"
                            .getBytes(StandardCharsets.UTF_8);
                    return exchange.getResponse().writeWith(
                            Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
                });
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
