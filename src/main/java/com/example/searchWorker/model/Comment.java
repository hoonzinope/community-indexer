package com.example.searchWorker.model;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class Comment {
    private long comment_seq;
    private long post_seq;
    private String content;
    private LocalDateTime insert_ts;
    private LocalDateTime delete_ts;
    private Integer delete_flag;
    private Long parent_comment_seq;
    private long user_seq;
}
