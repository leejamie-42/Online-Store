package com.comp5348.bank.service;

import com.comp5348.bank.dto.AccountDTO;
import com.comp5348.bank.enums.AccountType;
import com.comp5348.bank.exception.AccountNotFoundException;
import com.comp5348.bank.exception.UnauthorizedAccessException;
import com.comp5348.bank.model.Account;
import com.comp5348.bank.model.Customer;
import com.comp5348.bank.repository.AccountRepository;
import com.comp5348.bank.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public AccountDTO createAccount(Long customerId, String name, AccountType type) {
        log.info("Creating account for customer={}, name={}, type={}", customerId, name, type);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new AccountNotFoundException("Customer not found"));

        Account account = new Account(customer, name, type);
        account = accountRepository.save(account);
        log.info("Account created: id={}", account.getId());
        return new AccountDTO(account);
    }

    public AccountDTO getAccount(Long customerId, Long accountId) {
        log.info("Getting account={} for customer={}", accountId, customerId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (!account.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedAccessException("Customer does not own this account");
        }

        return new AccountDTO(account);
    }

}
