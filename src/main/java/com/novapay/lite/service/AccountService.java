package com.novapay.lite.service;

import com.novapay.lite.model.Account;
import com.novapay.lite.model.Customer;
import com.novapay.lite.repository.AccountRepository;
import com.novapay.lite.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AuditService auditService;

    @Transactional
    public Account createAccount(Long customerId, String currency, String correlationId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));

        String accountNumber = "ACC-" + System.currentTimeMillis();
        
        Account account = Account.builder()
                .customer(customer)
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .currency(currency)
                .build();
                
        Account savedAccount = accountRepository.save(account);
        auditService.logEvent("ACCOUNT_CREATED", "Created account " + accountNumber + " for customer " + customerId, correlationId);
        return savedAccount;
    }

    public Optional<Account> getAccount(Long id) {
        return accountRepository.findById(id);
    }
    
    public Optional<Account> getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber);
    }
}
