package com.novapay.lite.service;

import com.novapay.lite.model.Customer;
import com.novapay.lite.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void createCustomer_setsEncryptedEmailDuringExpandMigratePhase() {
        Customer customer = Customer.builder()
                .firstName("Asha")
                .lastName("Rao")
                .email("asha@example.com")
                .build();
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> {
            Customer saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        Customer result = customerService.createCustomer(customer, "corr-123");

        assertEquals("ENCRYPTED_asha@example.com", result.getEncryptedEmail());
        verify(auditService).logEvent(eq("CUSTOMER_CREATED"), eq("Created customer with ID: 10"), eq("corr-123"));
    }

    @Test
    void createCustomer_preservesExistingEncryptedEmail() {
        Customer customer = Customer.builder()
                .firstName("Asha")
                .lastName("Rao")
                .email("asha@example.com")
                .encryptedEmail("ciphertext")
                .build();
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = customerService.createCustomer(customer, "corr-123");

        assertEquals("ciphertext", result.getEncryptedEmail());
    }

    @Test
    void createCustomer_allowsNullEmailForBackwardCompatibility() {
        Customer customer = Customer.builder()
                .firstName("Asha")
                .lastName("Rao")
                .build();
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Customer result = customerService.createCustomer(customer, "corr-123");

        assertNull(result.getEncryptedEmail());
    }
}
