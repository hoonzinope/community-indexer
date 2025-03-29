package com.example.searchWorker.worker;

import com.example.searchWorker.service.MessageProduceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Producer {

    @Autowired
    private MessageProduceService messageProduceService;

    @Scheduled(fixedRate = 5000)
    public void produce() {
        messageProduceService.produce();
    }

    @Scheduled(fixedRate = 10000)
    public void produceFailed() {
        messageProduceService.produceFailed();
    }
}
