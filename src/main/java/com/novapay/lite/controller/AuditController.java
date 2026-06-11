package com.novapay.lite.controller;

import com.novapay.lite.model.AuditEvent;
import com.novapay.lite.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    @GetMapping("/events")
    public ResponseEntity<List<AuditEvent>> getAuditEvents() {
        return ResponseEntity.ok(auditService.getAllEvents());
    }
}
