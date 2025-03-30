package com.example.searchWorker.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MessageProduceServiceTest {

    @Autowired
    private MessageProduceService messageProduceService;

    @Test
    @Rollback(false)
    void produceTest() {
        messageProduceService.produce();
    }
}