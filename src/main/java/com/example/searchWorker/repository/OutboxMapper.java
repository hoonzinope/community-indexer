package com.example.searchWorker.repository;

import com.example.searchWorker.model.OutBox;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OutboxMapper {
    public List<OutBox> getOutboxData();
    public List<OutBox> getFailedOutboxData();
    public void updateOutboxStatus(OutBox outbox);
}
