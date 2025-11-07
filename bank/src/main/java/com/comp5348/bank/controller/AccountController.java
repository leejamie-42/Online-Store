package com.comp5348.bank.controller;

import com.comp5348.bank.dto.AccountDTO;
import com.comp5348.bank.enums.AccountType;
import com.comp5348.bank.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer/{customerId}/account")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountDTO> createAccount(
            @PathVariable Long customerId,
            @RequestParam String name,
            @RequestParam AccountType type) {

        log.info("Creating account for customer={}, type={}", customerId, type);
        AccountDTO account = accountService.createAccount(customerId, name, type);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountDTO> getAccount(
            @PathVariable Long customerId,
            @PathVariable Long accountId) {

        log.info("Getting account={} for customer={}", accountId, customerId);
        AccountDTO account = accountService.getAccount(customerId, accountId);
        return ResponseEntity.ok(account);
    }
}
