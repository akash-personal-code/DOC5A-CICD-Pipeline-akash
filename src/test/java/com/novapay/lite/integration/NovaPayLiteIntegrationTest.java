package com.novapay.lite.integration;

import com.novapay.lite.model.Account;
import com.novapay.lite.model.Customer;
import com.novapay.lite.model.Payment;
import com.novapay.lite.repository.AccountRepository;
import com.novapay.lite.repository.CustomerRepository;
import com.novapay.lite.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class NovaPayLiteIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    static RabbitMQContainer rabbitmq = new RabbitMQContainer("rabbitmq:3.13-management-alpine");

    // We can use a simple Redis container using GenericContainer
    @Container
    static org.testcontainers.containers.GenericContainer<?> redis = new org.testcontainers.containers.GenericContainer<>("redis:7-alpine").withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.rabbitmq.host", rabbitmq::getHost);
        registry.add("spring.rabbitmq.port", rabbitmq::getAmqpPort);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    void testFullPaymentFlow() {
        // 1. Create Customer
        Map<String, String> customerReq = Map.of(
                "firstName", "Jane",
                "lastName", "Smith",
                "email", "jane@example.com"
        );
        ResponseEntity<Customer> customerRes = restTemplate.postForEntity("/api/customers", customerReq, Customer.class);
        assertEquals(HttpStatus.OK, customerRes.getStatusCode());
        Customer createdCustomer = customerRes.getBody();
        assertNotNull(createdCustomer);

        // 2. Create Account
        Map<String, Object> accountReq = Map.of(
                "customerId", createdCustomer.getId(),
                "currency", "USD"
        );
        ResponseEntity<Account> accountRes = restTemplate.postForEntity("/api/accounts", accountReq, Account.class);
        assertEquals(HttpStatus.OK, accountRes.getStatusCode());
        Account createdAccount = accountRes.getBody();
        assertNotNull(createdAccount);

        // Add funds manually for test
        createdAccount.setBalance(new BigDecimal("1000.00"));
        accountRepository.save(createdAccount);

        // 3. Initiate Payment
        Map<String, Object> paymentReq = Map.of(
                "sourceAccountId", createdAccount.getId(),
                "destinationAccountNumber", "EXT-9999",
                "amount", 150.00,
                "currency", "USD",
                "idempotencyKey", "unique-payment-123"
        );
        ResponseEntity<String> paymentRes = restTemplate.postForEntity("/api/payments", paymentReq, String.class);
        assertEquals(HttpStatus.OK, paymentRes.getStatusCode(), "Payment API response: " + paymentRes.getBody());

        Payment createdPayment = paymentRepository.findByIdempotencyKey("unique-payment-123")
                .orElseThrow();

        assertNotNull(createdPayment);
        assertEquals(0, new BigDecimal("150.00").compareTo(createdPayment.getAmount()));
        // 4. Verify Balance
        Account updatedAccount = accountRepository.findById(createdAccount.getId()).get();
        assertEquals(0, new BigDecimal("850.00").compareTo(updatedAccount.getBalance()));
    }
}
