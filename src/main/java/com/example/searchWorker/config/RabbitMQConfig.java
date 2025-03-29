package com.example.searchWorker.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// RabbitMQ configuration
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.name}")
    private String queueName;

    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    public String getQueueName() {
        return queueName;
    }
    public String getExchangeName() {
        return exchangeName;
    }
    public String getRoutingKey() {
        return routingKey;
    }

    @Bean
    public Queue rabbitQueue() {
        return new Queue(queueName, false); // 내구성이 필요하면 true 설정
    }

    @Bean
    public TopicExchange rabbitExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding rabbitBinding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }
}
