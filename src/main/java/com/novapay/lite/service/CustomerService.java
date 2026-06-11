package com.novapay.lite.service;

import com.novapay.lite.model.Customer;
import com.novapay.lite.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    @Transactional
    public Customer createCustomer(Customer customer, String correlationId) {
        // Handle migration logic safely
        if (customer.getEmail() != null && customer.getEncryptedEmail() == null) {
            customer.setEncryptedEmail("ENCRYPTED_" + customer.getEmail());
        }
        
        Customer savedCustomer = customerRepository.save(customer);
        auditService.logEvent("CUSTOMER_CREATED", "Created customer with ID: " + savedCustomer.getId(), correlationId);
        return savedCustomer;
    }

    public Optional<Customer> getCustomer(Long id) {
        return customerRepository.findById(id);
    }
}
