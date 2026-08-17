package com.jiawa.train.business.mq;

import com.jiawa.train.business.config.RabbitConfig;
import com.jiawa.train.business.service.ConfirmOrderService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 异步抢票消费者 —— 削峰后真正执行锁票/扣库存/支付。
 */
@Component
@ConditionalOnProperty(name = "train.mq.type", havingValue = "rabbit", matchIfMissing = true)
public class ConfirmOrderConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(ConfirmOrderConsumer.class);

    @Resource
    private ConfirmOrderService confirmOrderService;

    @RabbitListener(queues = RabbitConfig.CONFIRM_ORDER_QUEUE)
    public void onMessage(ConfirmOrderMessage message) {
        LOG.info("消费抢票消息 orderId={}", message.getOrderId());
        try {
            confirmOrderService.processConfirm(message.getOrderId(), message.getReq());
        } catch (Exception e) {
            LOG.error("抢票消息处理失败 orderId={}", message.getOrderId(), e);
        }
    }
}
