package com.example.searchWorker.dao;

import com.example.searchWorker.model.OutBox;
import com.example.searchWorker.repository.OutboxMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OutboxDAO {

    // OutboxMapper를 주입받아 사용할 수 있도록 설정
    private final OutboxMapper outboxMapper;

    @Autowired
    public OutboxDAO(OutboxMapper outboxMapper) {
        this.outboxMapper = outboxMapper;
    }

    // Outbox 데이터를 가져오는 메서드
    public List<OutBox> getOutboxData() {
        return outboxMapper.getOutboxData();
    }

    // 실패한 Outbox 데이터를 가져오는 메서드
    public List<OutBox> getFailedOutboxData() {
        return outboxMapper.getFailedOutboxData();
    }

    // Outbox 상태를 업데이트하는 메서드
    public void updateOutboxStatus(OutBox outbox) {
        outboxMapper.updateOutboxStatus(outbox);
    }
}
