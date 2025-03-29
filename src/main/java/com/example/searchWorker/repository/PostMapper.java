package com.example.searchWorker.repository;

import com.example.searchWorker.model.Post;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface PostMapper {
    Optional<Post> getPostById(Long post_seq);
    List<Post> getPostByIdList(List<Long> post_seq_list);
}
