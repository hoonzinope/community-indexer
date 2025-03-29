package com.example.searchWorker.service;

import com.example.searchWorker.config.RabbitMQConfig;
import com.example.searchWorker.dao.CommentDAO;
import com.example.searchWorker.dao.OutboxDAO;
import com.example.searchWorker.dao.PostDAO;
import com.example.searchWorker.model.OutBox;
import com.rabbitmq.client.AMQP;
import org.json.simple.JSONObject;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessageProduceService {

    @Autowired
    private OutboxDAO outboxDAO;

    @Autowired
    private PostDAO postDAO;

    @Autowired
    private CommentDAO commentDAO;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RabbitMQConfig rabbitMQConfig;

    public void produce() {
        outboxDAO.getOutboxData()
                // outbox에 대한 처리 로직
                .forEach(this::produceMessage);
    }

    public void produceFailed() {
        outboxDAO.getFailedOutboxData()
                // outbox에 대한 처리 로직
                .forEach(this::produceMessage);
    }

    private void produceMessage(OutBox outbox) {
        try {
            rabbitTemplate.convertAndSend(
                    rabbitMQConfig.getExchangeName(),
                    rabbitMQConfig.getRoutingKey(),
                    convertToJson(outbox)
            );
            // outbox 상태 업데이트
            outbox.setStatus("COMPLETED");
            outboxDAO.updateOutboxStatus(outbox);
        } catch (Exception e) {
            // 예외 발생 시 outbox 상태를 FAILED로 업데이트
            outbox.setStatus("FAILED");
            outboxDAO.updateOutboxStatus(outbox);
        }
    }

    private JSONObject convertToJson(OutBox outbox) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", outbox.getId());
        jsonObject.put("aggregate_type", outbox.getAggregate_type());
        jsonObject.put("aggregate_id", outbox.getAggregate_id());
        jsonObject.put("event_type", outbox.getEvent_type());
        jsonObject.put("payload", outbox.getPayload());
        jsonObject.put("created_ts", outbox.getCreated_ts());
        jsonObject.put("processed_ts", outbox.getProcessed_ts());
        jsonObject.put("status", outbox.getStatus());
        return jsonObject;
    }

}
