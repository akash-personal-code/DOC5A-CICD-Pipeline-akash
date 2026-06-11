package com.novapay.lite.service;

import com.novapay.lite.config.RabbitMQConfig;
import com.novapay.lite.model.Account;
import com.novapay.lite.model.Payment;
import com.novapay.lite.repository.AccountRepository;
import com.novapay.lite.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountRepository accountRepository;
    private final AuditService auditService;
    private final RabbitTemplate rabbitTemplate;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public Payment initiatePayment(Long sourceAccountId, String destinationAccount, BigDecimal amount, String currency, String idempotencyKey, String correlationId) {
        
        // Check Redis for idempotency
        Boolean isNewRequest = redisTemplate.opsForValue().setIfAbsent("payment:idempotency:" + idempotencyKey, "PROCESSING", Duration.ofHours(24));
        
        if (Boolean.FALSE.equals(isNewRequest)) {
            log.info("Idempotent request received: {}", idempotencyKey);
            return paymentRepository.findByIdempotencyKey(idempotencyKey)
                    .orElseThrow(() -> new IllegalStateException("Payment with idempotency key found in Redis but not in DB"));
        }

        Account sourceAccount = accountRepository.findById(sourceAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        // Deduct balance
        sourceAccount.setBalance(sourceAccount.getBalance().subtract(amount));
        accountRepository.save(sourceAccount);

        Payment payment = Payment.builder()
                .sourceAccount(sourceAccount)
                .destinationAccountNumber(destinationAccount)
                .amount(amount)
                .currency(currency)
                .status("COMPLETED")
                .idempotencyKey(idempotencyKey)
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        
        auditService.logEvent("PAYMENT_INITIATED", "Payment " + savedPayment.getId() + " initiated for amount " + amount, correlationId);

        // Publish event to RabbitMQ
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.PAYMENT_INITIATED_ROUTING_KEY, savedPayment);
        } catch (Exception e) {
            log.error("Failed to publish payment initiated event", e);
            // In a real system, we'd use the Outbox pattern to guarantee delivery.
        }

        return savedPayment;
    }

    public Optional<Payment> getPayment(Long id) {
        return paymentRepository.findById(id);
    }
}
