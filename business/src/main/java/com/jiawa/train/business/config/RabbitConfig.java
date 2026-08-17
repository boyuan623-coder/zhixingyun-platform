package com.jiawa.train.business.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 抢票下单削峰：confirm.order 队列。
 */
@Configuration
@ConditionalOnProperty(name = "train.mq.type", havingValue = "rabbit", matchIfMissing = true)
public class RabbitConfig {

    public static final String CONFIRM_ORDER_EXCHANGE = "confirm.order.exchange";
    public static final String CONFIRM_ORDER_QUEUE = "confirm.order.queue";
    public static final String CONFIRM_ORDER_ROUTING_KEY = "confirm.order";

    @Bean
    public DirectExchange confirmOrderExchange() {
        return new DirectExchange(CONFIRM_ORDER_EXCHANGE, true, false);
    }

    @Bean
    public Queue confirmOrderQueue() {
        return new Queue(CONFIRM_ORDER_QUEUE, true);
    }

    @Bean
    public Binding confirmOrderBinding() {
        return BindingBuilder.bind(confirmOrderQueue())
                .to(confirmOrderExchange())
                .with(CONFIRM_ORDER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
