package com.novapay.lite.controller;

import com.novapay.lite.model.Payment;
import com.novapay.lite.service.PaymentService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final MeterRegistry meterRegistry;

    @PostMapping
    public ResponseEntity<PaymentResponse> initiatePayment(@Valid @RequestBody PaymentRequest request) {
        String correlationId = MDC.get("correlationId");

        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Payment payment = paymentService.initiatePayment(
                    request.getSourceAccountId(),
                    request.getDestinationAccountNumber(),
                    request.getAmount(),
                    request.getCurrency(),
                    request.getIdempotencyKey(),
                    correlationId);
            sample.stop(meterRegistry.timer("payment.processing.latency", "status", "success"));
            meterRegistry.counter("payment.count", "status", "success").increment();
            return ResponseEntity.ok(PaymentResponse.from(payment));
        } catch (Exception e) {
            sample.stop(meterRegistry.timer("payment.processing.latency", "status", "failure"));
            meterRegistry.counter("payment.count", "status", "failure").increment();
            throw e;
        }
    }

    public record PaymentResponse(
            Long id,
            Long sourceAccountId,
            String destinationAccountNumber,
            BigDecimal amount,
            String currency,
            String status,
            String idempotencyKey,
            Instant createdAt) {
        public static PaymentResponse from(Payment payment) {
            return new PaymentResponse(
                    payment.getId(),
                    payment.getSourceAccount().getId(),
                    payment.getDestinationAccountNumber(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getStatus(),
                    payment.getIdempotencyKey(),
                    payment.getCreatedAt());
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long paymentId) {
        return paymentService.getPayment(paymentId)
                .map(PaymentResponse::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Data
    public static class PaymentRequest {
        @NotNull(message = "Source account ID is required")
        private Long sourceAccountId;

        @NotBlank(message = "Destination account number is required")
        private String destinationAccountNumber;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        private BigDecimal amount;

        @NotBlank(message = "Currency is required")
        private String currency;

        @NotBlank(message = "Idempotency key is required")
        private String idempotencyKey;
    }
}
