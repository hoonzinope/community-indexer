package com.example.searchWorker.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class Post {
    private long post_seq;
    private String title;
    private String content;
    private LocalDateTime insert_ts;
    private LocalDateTime update_ts;
    private int view_count;
    private LocalDateTime delete_ts;
    private boolean delete_flag;
    private long user_seq;
    private long subject_seq;
}
