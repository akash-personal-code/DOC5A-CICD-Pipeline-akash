package com.novapay.lite.controller;

import com.novapay.lite.model.Account;
import com.novapay.lite.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody AccountRequest request) {
        String correlationId = MDC.get("correlationId");
        Account account = accountService.createAccount(request.getCustomerId(), request.getCurrency(), correlationId);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/{accountId}/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable Long accountId) {
        return accountService.getAccount(accountId)
                .map(acc -> ResponseEntity.ok(Map.<String, Object>of(
                        "accountId", acc.getId(),
                        "accountNumber", acc.getAccountNumber(),
                        "balance", acc.getBalance(),
                        "currency", acc.getCurrency()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    @Data
    public static class AccountRequest {
        @NotNull(message = "Customer ID is required")
        private Long customerId;

        @NotBlank(message = "Currency is required")
        private String currency;
    }
}
