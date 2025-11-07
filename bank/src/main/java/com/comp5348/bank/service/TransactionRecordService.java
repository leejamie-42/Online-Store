package com.comp5348.bank.service;

import com.comp5348.bank.dto.TransactionRecordDTO;
import com.comp5348.bank.enums.TransactionStatus;
import com.comp5348.bank.exception.AccountNotFoundException;
import com.comp5348.bank.exception.InsufficientFundsException;
import com.comp5348.bank.exception.UnauthorizedAccessException;
import com.comp5348.bank.model.Account;
import com.comp5348.bank.model.TransactionRecord;
import com.comp5348.bank.repository.AccountRepository;
import com.comp5348.bank.repository.TransactionRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.comp5348.bank.exception.NegativeTransferAmountException;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionRecordService {

    private final AccountRepository accountRepository;
    private final TransactionRecordRepository transactionRepository;

    @Transactional
    public TransactionRecordDTO performTransaction(
            Long fromCustomerId, Long fromAccountId,
            Long toCustomerId, Long toAccountId,
            BigDecimal amount, String memo) {

        log.info("Performing transaction: from={}:{}, to={}:{}, amount={}",
                fromCustomerId, fromAccountId, toCustomerId, toAccountId, amount);
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegativeTransferAmountException();
        }

        // Validate accounts
        Account fromAccount = null;
        Account toAccount = null;

        if (fromAccountId != null) {
            fromAccount = accountRepository.findById(fromAccountId)
                    .orElseThrow(() -> new AccountNotFoundException("From account not found"));
            if (fromCustomerId != null && !fromAccount.getCustomer().getId().equals(fromCustomerId)) {
                throw new UnauthorizedAccessException("Customer does not own from account");
            }
        }

        if (toAccountId != null) {
            toAccount = accountRepository.findById(toAccountId)
                    .orElseThrow(() -> new AccountNotFoundException("To account not found"));
            if (toCustomerId != null && !toAccount.getCustomer().getId().equals(toCustomerId)) {
                throw new UnauthorizedAccessException("Customer does not own to account");
            }
        }

        // Perform transaction
        TransactionRecord transaction = new TransactionRecord(amount, memo, fromAccount, toAccount,
                TransactionStatus.processing);

        // Update balances
        if (fromAccount != null) {
            if (fromAccount.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException("Insufficient balance");
            }
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            accountRepository.save(fromAccount);
            log.info("Debited {} from account {}", amount, fromAccountId);
        }

        if (toAccount != null) {
            toAccount.setBalance(toAccount.getBalance().add(amount));
            accountRepository.save(toAccount);
            log.info("Credited {} to account {}", amount, toAccountId);
        }

        transaction.setStatus(TransactionStatus.completed);
        transaction = transactionRepository.save(transaction);

        log.info("Transaction completed: id={}", transaction.getId());
        return new TransactionRecordDTO(transaction);
    }

    public List<TransactionRecordDTO> getTransactionHistory(Long customerId, Long accountId) {
        log.info("Getting transaction history for customer={}, account={}", customerId, accountId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        if (!account.getCustomer().getId().equals(customerId)) {
            throw new UnauthorizedAccessException("Customer does not own this account");
        }

        List<TransactionRecord> transactions = transactionRepository
                .findByToAccountIdOrFromAccountId(accountId, accountId);

        return transactions.stream()
                .map(TransactionRecordDTO::new)
                .collect(Collectors.toList());
    }
}
