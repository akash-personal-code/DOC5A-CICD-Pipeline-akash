package com.novapay.lite.service;

import com.novapay.lite.model.AuditEvent;
import com.novapay.lite.repository.AuditEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    @Transactional
    public AuditEvent logEvent(String eventType, String details, String correlationId) {
        AuditEvent event = AuditEvent.builder()
                .eventType(eventType)
                .details(details)
                .correlationId(correlationId)
                .build();
        return auditEventRepository.save(event);
    }

    public List<AuditEvent> getAllEvents() {
        return auditEventRepository.findAll();
    }
}
