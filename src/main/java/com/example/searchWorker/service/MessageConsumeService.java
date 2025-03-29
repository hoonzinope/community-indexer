package com.example.searchWorker.service;

import com.example.searchWorker.dao.CommentDAO;
import com.example.searchWorker.dao.PostDAO;
import com.example.searchWorker.model.Comment;
import com.example.searchWorker.model.OutBox;
import com.example.searchWorker.model.Post;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class MessageConsumeService {

    @Value("${spring.elasticsearch.uris}")
    private String es_url;

    @Autowired
    private PostDAO postDAO;

    @Autowired
    private CommentDAO commentDAO;

    // Outbox -> Post, Comment -> searchEngine
    public void consume(OutBox outbox) {
        if(outbox == null) {
            throw new IllegalArgumentException("Outbox cannot be null");
        }
        if(outbox.getAggregate_type() == null || outbox.getAggregate_id() == null) {
            throw new IllegalArgumentException("Outbox type and id cannot be null");
        }
        switch (outbox.getAggregate_type()) {
            case "POST":
                consumePost(outbox);
                break;
            case "COMMENT":
                consumeComment(outbox);
                break;
            default:
                throw new IllegalArgumentException("Unknown type: " + outbox.getAggregate_type());
        }
    }

    public void consumePost(OutBox outbox) {
        // Post 처리 로직
        Long post_seq = outbox.getAggregate_id();
        Post post = postDAO.getPostById(post_seq);

        if (post == null) {
            throw new IllegalArgumentException("Post not found with id: " + post_seq);
        }

        // title, content, post_seq, delete_flag, subject_seq, user_seq
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("title", post.getTitle());
        jsonObject.put("content", post.getContent());
        jsonObject.put("post_seq", post.getPost_seq());
        jsonObject.put("delete_flag", post.isDelete_flag());
        jsonObject.put("subject_seq", post.getSubject_seq());
        jsonObject.put("user_seq", post.getUser_seq());

        // Elasticsearch에 데이터 전송 로직
        String ES_URL = es_url + "/posts/post/" + post.getPost_seq();
        // HttpClient 생성
        sendToSearchEngine(jsonObject, ES_URL);

    }

    public void consumeComment(OutBox outbox) {
        // Comment 처리 로직
        Long comment_seq = outbox.getAggregate_id();
        Comment comment = commentDAO.getCommentById(comment_seq);

        // content, comment_seq, delete_flag, post_seq, user_seq, parent_comment_seq, insert_ts, delete_ts
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("content", comment.getContent());
        jsonObject.put("comment_seq", comment_seq);
        jsonObject.put("delete_flag", comment.getDelete_flag());
        jsonObject.put("post_seq", comment.getPost_seq());
        jsonObject.put("user_seq", comment.getUser_seq());
        jsonObject.put("parent_comment_seq", comment.getParent_comment_seq());
        jsonObject.put("insert_ts", comment.getInsert_ts());
        jsonObject.put("delete_ts", comment.getDelete_ts());

        // Elasticsearch에 데이터 전송 로직
        String ES_URL = es_url + "/comments/comment/" + comment.getComment_seq();

        // HttpClient 생성
        sendToSearchEngine(jsonObject, ES_URL);
    }

    private void sendToSearchEngine(JSONObject jsonObject, String ES_URL) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            // HTTP POST 요청 생성
            HttpPost httpPost = new HttpPost(ES_URL);

            // 인덱싱할 JSON 문서 생성
            StringEntity entity = new StringEntity(jsonObject.toJSONString(), "UTF-8");
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-Type", "application/json");

            // 요청 실행
            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                System.out.println("응답 상태: " + response.getStatusLine());

                HttpEntity responseEntity = response.getEntity();
                if (responseEntity != null) {
                    String responseString = EntityUtils.toString(responseEntity, "UTF-8");
                    System.out.println("응답 내용: " + responseString);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
