package com.example.searchWorker.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OutBox {
    Long id;
    String aggregate_type;
    Long aggregate_id;
    String event_type;
    String payload;
    LocalDateTime created_ts;
    LocalDateTime processed_ts;
    String status; // PENDING, COMPLETED, FAILED
}
