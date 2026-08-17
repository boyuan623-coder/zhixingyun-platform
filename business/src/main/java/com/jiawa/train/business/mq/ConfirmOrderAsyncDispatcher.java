package com.jiawa.train.business.mq;

import com.jiawa.train.business.config.RabbitConfig;
import com.jiawa.train.business.service.ConfirmOrderService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 下单削峰分发：优先 RabbitMQ；配置 local 或 broker 不可用时回退本地有界队列。
 */
@Component
public class ConfirmOrderAsyncDispatcher {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderAsyncDispatcher.class);

    @Resource
    private ObjectProvider<RabbitTemplate> rabbitTemplateProvider;
    @Resource
    @Lazy
    private ConfirmOrderService confirmOrderService;

    @Value("${train.mq.type:rabbit}")
    private String mqType;

    private final ThreadPoolExecutor localExecutor = new ThreadPoolExecutor(
            4, 8, 60, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(2000),
            r -> {
                Thread t = new Thread(r, "confirm-order-local-async");
                t.setDaemon(true);
                return t;
            },
            new ThreadPoolExecutor.CallerRunsPolicy());

    public void dispatch(ConfirmOrderMessage message) {
        if ("local".equalsIgnoreCase(mqType)) {
            dispatchLocal(message);
            return;
        }
        RabbitTemplate rabbitTemplate = rabbitTemplateProvider.getIfAvailable();
        if (rabbitTemplate == null) {
            LOG.warn("RabbitTemplate 未装配，回退本地队列 orderId={}", message.getOrderId());
            dispatchLocal(message);
            return;
        }
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.CONFIRM_ORDER_EXCHANGE,
                    RabbitConfig.CONFIRM_ORDER_ROUTING_KEY,
                    message);
            LOG.info("已发送 RabbitMQ orderId={}", message.getOrderId());
        } catch (Exception e) {
            LOG.warn("RabbitMQ 不可用，回退本地队列 orderId={} err={}", message.getOrderId(), e.getMessage());
            dispatchLocal(message);
        }
    }

    private void dispatchLocal(ConfirmOrderMessage message) {
        localExecutor.execute(() -> {
            try {
                confirmOrderService.processConfirm(message.getOrderId(), message.getReq());
            } catch (Exception ex) {
                LOG.error("本地异步购票失败 orderId={}", message.getOrderId(), ex);
            }
        });
    }
}
