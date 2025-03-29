package com.example.searchWorker.dao;

import com.example.searchWorker.model.Post;
import com.example.searchWorker.repository.PostMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PostDAO {

    private final PostMapper postMapper;

    @Autowired
    public PostDAO(PostMapper postMapper) {
        this.postMapper = postMapper;
    }

    public Post getPostById(Long post_seq) {
        Optional<Post> postOptional = postMapper.getPostById(post_seq);
        if (postOptional.isPresent()) {
            return postOptional.get();
        } else {
            // or handle the case when the post is not found
            throw new RuntimeException("not exist post");
        }
    }

    public List<Post> getPostByIdList(List<Long> post_seq_list) {
        return postMapper.getPostByIdList(post_seq_list);
    }
}
