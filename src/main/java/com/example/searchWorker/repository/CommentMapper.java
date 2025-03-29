package com.example.searchWorker.repository;

import com.example.searchWorker.model.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CommentMapper {
    Optional<Comment> getCommentById(Long comment_seq);
    List<Comment> getCommentByIdList(List<Long> comment_seq_list);
}
