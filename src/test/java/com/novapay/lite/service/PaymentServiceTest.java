package com.novapay.lite.service;

import com.novapay.lite.model.Account;
import com.novapay.lite.model.Payment;
import com.novapay.lite.repository.AccountRepository;
import com.novapay.lite.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AuditService auditService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void initiatePayment_success() {
        String idempotencyKey = "key123";
        Long sourceAccountId = 1L;
        Account sourceAccount = new Account();
        sourceAccount.setId(sourceAccountId);
        sourceAccount.setBalance(new BigDecimal("100.00"));

        when(valueOperations.setIfAbsent(eq("payment:idempotency:" + idempotencyKey), eq("PROCESSING"), any())).thenReturn(true);
        when(accountRepository.findById(sourceAccountId)).thenReturn(Optional.of(sourceAccount));
        
        Payment savedPayment = new Payment();
        savedPayment.setId(1L);
        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);

        Payment result = paymentService.initiatePayment(sourceAccountId, "DEST-123", new BigDecimal("50.00"), "USD", idempotencyKey, "corr-1");

        assertNotNull(result);
        assertEquals(new BigDecimal("50.00"), sourceAccount.getBalance()); // Balance deducted
        verify(paymentRepository).save(any(Payment.class));
        verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Payment.class));
    }

    @Test
    void initiatePayment_insufficientFunds() {
        String idempotencyKey = "key123";
        Long sourceAccountId = 1L;
        Account sourceAccount = new Account();
        sourceAccount.setId(sourceAccountId);
        sourceAccount.setBalance(new BigDecimal("10.00"));

        when(valueOperations.setIfAbsent(eq("payment:idempotency:" + idempotencyKey), eq("PROCESSING"), any())).thenReturn(true);
        when(accountRepository.findById(sourceAccountId)).thenReturn(Optional.of(sourceAccount));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.initiatePayment(sourceAccountId, "DEST-123", new BigDecimal("50.00"), "USD", idempotencyKey, "corr-1");
        });

        assertEquals("Insufficient funds", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void initiatePayment_idempotent() {
        String idempotencyKey = "key123";
        
        when(valueOperations.setIfAbsent(eq("payment:idempotency:" + idempotencyKey), eq("PROCESSING"), any())).thenReturn(false);
        Payment existingPayment = new Payment();
        existingPayment.setId(2L);
        when(paymentRepository.findByIdempotencyKey(idempotencyKey)).thenReturn(Optional.of(existingPayment));

        Payment result = paymentService.initiatePayment(1L, "DEST-123", new BigDecimal("50.00"), "USD", idempotencyKey, "corr-1");

        assertNotNull(result);
        assertEquals(2L, result.getId());
        verify(accountRepository, never()).findById(anyLong());
        verify(paymentRepository, never()).save(any(Payment.class));
    }
    @Test
    void initiatePayment_continuesWhenRabbitMqPublishFails() {
        String idempotencyKey = "mq-failure-key";
        Long sourceAccountId = 1L;
        Account sourceAccount = new Account();
        sourceAccount.setId(sourceAccountId);
        sourceAccount.setBalance(new BigDecimal("100.00"));

        when(valueOperations.setIfAbsent(eq("payment:idempotency:" + idempotencyKey), eq("PROCESSING"), any())).thenReturn(true);
        when(accountRepository.findById(sourceAccountId)).thenReturn(Optional.of(sourceAccount));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(99L);
            return payment;
        });
        doThrow(new RuntimeException("RabbitMQ unavailable"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Payment.class));

        Payment result = paymentService.initiatePayment(sourceAccountId, "DEST-999", new BigDecimal("25.00"), "USD", idempotencyKey, "corr-mq");

        assertNotNull(result);
        assertEquals(99L, result.getId());
        assertEquals(new BigDecimal("75.00"), sourceAccount.getBalance());
        verify(paymentRepository).save(any(Payment.class));
    }

}
