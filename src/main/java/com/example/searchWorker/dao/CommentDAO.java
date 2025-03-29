package com.example.searchWorker.dao;

import com.example.searchWorker.model.Comment;
import com.example.searchWorker.repository.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentDAO {

    private CommentMapper commentMapper;

    @Autowired
    public CommentDAO(CommentMapper commentMapper) {
        this.commentMapper = commentMapper;
    }

    public Comment getCommentById(Long comment_seq) {
        return commentMapper.getCommentById(comment_seq)
                .orElseThrow(() -> new RuntimeException("not exist comment"));
    }

    public List<Comment> getCommentByIdList(List<Long> comment_seq_list) {
        return commentMapper.getCommentByIdList(comment_seq_list);
    }
}
