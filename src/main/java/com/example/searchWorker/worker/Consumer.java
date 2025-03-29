package com.example.searchWorker.worker;

import com.example.searchWorker.config.RabbitMQConfig;
import com.example.searchWorker.model.OutBox;
import com.example.searchWorker.service.MessageConsumeService;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.io.IOException;

@Component
public class Consumer {

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private MessageConsumeService messageConsumeService;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void consume(OutBox outbox, Channel channel, Message message) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            // 메시지 처리 로직 실행 (비즈니스 로직)
            messageConsumeService.consume(outbox);
            // 처리 성공 시 메시지 ACK 전송
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 예외 발생 시, 로그 출력
            System.err.println("메시지 처리 실패: " + e.getMessage());
            try {
                // 재시도 정책이 적용되더라도, 최종 실패 시 DLQ로 보내기 위해 requeue=false로 설정
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
            // 재시도 실패 후, DLQ로 이동시키기 위해 예외 던짐
            throw new AmqpRejectAndDontRequeueException("Processing failed, sending to DLQ", e);
        }
    }
}
