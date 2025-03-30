package com.example.searchWorker.worker;

import com.example.searchWorker.config.RabbitMQConfig;
import com.example.searchWorker.model.OutBox;
import com.example.searchWorker.service.MessageConsumeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.Channel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class Consumer {

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    @Autowired
    private MessageConsumeService messageConsumeService;

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void consume(@Payload JSONObject msg, Message message, Channel channel){
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
//            // 메시지 바디를 문자열로 변환
//            String messageBody = new String(message.getBody());
//
//            // 문자열을 JSONObject로 파싱
//            JSONParser parser = new JSONParser();
//            JSONObject msg = (JSONObject) parser.parse(messageBody);
            System.out.println("Received message: " + msg);

            OutBox outBox = convertToOutBox(msg);
            // 메시지 처리 로직 실행
            messageConsumeService.consume(outBox);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            // 예외 발생 시, 로그 출력
            e.printStackTrace();
            System.err.println("메시지 처리 실패: " + e.getMessage());
            try {
                // 재시도 정책이 적용되더라도, 최종 실패 시 DLQ로 보내기 위해 requeue=false로 설정
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ioException) {
                ioException.printStackTrace();
                System.err.println("메시지 Nack 실패: " + ioException.getMessage());
            }
            // 재시도 실패 후, DLQ로 이동시키기 위해 예외 던짐
            throw new AmqpRejectAndDontRequeueException("Processing failed, sending to DLQ", e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.dlx.queue.name}")
    public void consumeDLQ(@Payload JSONObject msg, Message message, Channel channel) {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
//            // 메시지 바디를 문자열로 변환
//            String messageBody = new String(message.getBody());
//
//            // 문자열을 JSONObject로 파싱
//            JSONParser parser = new JSONParser();
//            JSONObject msg = (JSONObject) parser.parse(messageBody);

            System.out.println("Received DLQ message: " + msg);
            OutBox outBox = convertToOutBox(msg);
            try {
                // 메시지 처리 로직 실행
                messageConsumeService.consume(outBox);
                channel.basicAck(deliveryTag, false);
            } catch (Exception e) {
                // DELETE 이벤트이고 404 오류인 경우, 성공으로 처리
                if (outBox.getEvent_type().equals("DELETE") && e.getMessage().contains("404")) {
                    System.out.println("404 발생: 이미 삭제된 문서입니다. 성공으로 처리합니다.");
                    channel.basicAck(deliveryTag, false);
                } else {
                    throw e; // 다른 예외는 그대로 던지기
                }
            }
        } catch (Exception e) {
            // 예외 발생 시, 로그 출력
            e.printStackTrace();
            System.err.println("메시지 처리 실패: " + e.getMessage());
            try {
                // 재시도 정책이 적용되더라도, 최종 실패 시 DLQ로 보내기 위해 requeue=false로 설정
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                ioException.printStackTrace();
                System.err.println("메시지 Nack 실패: " + ioException.getMessage());
            }
            // 재시도 실패 후, DLQ로 이동 시키기 위해 예외 던짐
            throw new AmqpRejectAndDontRequeueException("Processing failed, sending to DLQ", e);
        }
    }

    private OutBox convertToOutBox(JSONObject msg) {
        OutBox outBox = OutBox.builder()
                .id(msg.containsKey("id") ? ((Number) msg.get("id")).longValue() : null)
                .aggregate_id(msg.containsKey("aggregate_id") ? ((Number) msg.get("aggregate_id")).longValue() : null)
                .aggregate_type((String) msg.get("aggregate_type"))
                .event_type((String) msg.get("event_type"))
                .payload((String) msg.get("payload"))
                .status((String) msg.get("status"))
                .build();

        // 날짜 문자열을 직접 파싱
        if (msg.containsKey("created_ts") && msg.get("created_ts") != null) {
            String createdTsStr = msg.get("created_ts").toString();
            outBox.setCreated_ts(LocalDateTime.parse(createdTsStr));
        }

        if (msg.containsKey("processed_ts") && msg.get("processed_ts") != null
                && !msg.get("processed_ts").equals("null")) {
            String processedTsStr = msg.get("processed_ts").toString();
            outBox.setProcessed_ts(LocalDateTime.parse(processedTsStr));
        }
        // 추가적인 필드가 필요하다면 여기에 추가
        return outBox;
    }
}
