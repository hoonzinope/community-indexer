package com.example.searchWorker.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

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

    // DLQ용 DLX, 큐, 라우팅키
    @Value("${rabbitmq.dlx.queue.name}")
    public String DLQ;
    @Value("${rabbitmq.dlx.exchange.name}")
    public String DLX;
    @Value("${rabbitmq.dlx.routing.key}")
    public String DL_ROUTING_KEY;
    
    @Bean
    public Queue rabbitQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", DLX)
                .withArgument("x-dead-letter-routing-key", DL_ROUTING_KEY)
                .build();
    }

    @Bean
    public TopicExchange rabbitExchange() {
        return new TopicExchange(exchangeName);
    }

    @Bean
    public Binding rabbitBinding(@Qualifier("rabbitQueue") Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    // Dead Letter Exchange 설정
    @Bean
    public DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX);
    }

    // Dead Letter Queue 설정
    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    // DLQ 바인딩
    @Bean
    public Binding bindingDLQ(Queue deadLetterQueue, DirectExchange deadLetterExchange) {
        return BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange).with(DL_ROUTING_KEY);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        //rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        //factory.setMessageConverter(jsonMessageConverter());
        factory.setConnectionFactory(connectionFactory);

        // 재시도 템플릿 설정: 최대 3회 재시도, 재시도 간격 2초 예시
        RetryTemplate retryTemplate = new RetryTemplate();
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(2000); // 2초 간격
        retryTemplate.setBackOffPolicy(backOffPolicy);

        factory.setRetryTemplate(retryTemplate);

        // 기본적으로 재큐잉하지 않으면 DLQ로 전송됨
        factory.setDefaultRequeueRejected(false);

        // 수동 ACK 모드로 설정 (예시)
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);

        // 커스텀 에러 핸들러 (옵션)
        factory.setErrorHandler(t -> System.err.println("Listener error: " + t.getMessage()));
        return factory;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
