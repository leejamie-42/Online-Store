package com.comp5348.bank.controller;

import com.comp5348.bank.dto.DepositWithdrawRequest;
import com.comp5348.bank.dto.TransactionRecordDTO;
import com.comp5348.bank.dto.TransferRequest;
import com.comp5348.bank.service.TransactionRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer/{fromCustomerId}/account/{accountId}/transaction_record")
@RequiredArgsConstructor
@Slf4j
public class TransactionRecordController {

        private final TransactionRecordService transactionRecordService;

        @PostMapping("/transfer")
        public ResponseEntity<TransactionRecordDTO> transfer(
                        @PathVariable Long fromCustomerId,
                        @PathVariable("accountId") Long fromAccountId,
                        @Valid @RequestBody TransferRequest request) {

                log.info("Transfer request from customer={}, account={} to customer={}, account={}",
                                fromCustomerId, fromAccountId, request.getToCustomerId(), request.getToAccountId());

                TransactionRecordDTO transaction = transactionRecordService.performTransaction(
                                fromCustomerId, fromAccountId,
                                request.getToCustomerId(), request.getToAccountId(),
                                request.getAmount(), "Transfer");
                return ResponseEntity.ok(transaction);
        }

        @PostMapping("/deposit")
        public ResponseEntity<TransactionRecordDTO> deposit(
                        @PathVariable("fromCustomerId") Long toCustomerId,
                        @PathVariable("accountId") Long toAccountId,
                        @Valid @RequestBody DepositWithdrawRequest request) {

                log.info("Deposit request for customer={}, account={}, amount={}",
                                toCustomerId, toAccountId, request.getAmount());

                TransactionRecordDTO transaction = transactionRecordService.performTransaction(
                                null, null,
                                toCustomerId, toAccountId,
                                request.getAmount(), "Deposit");
                return ResponseEntity.ok(transaction);
        }

        @PostMapping("/withdraw")
        public ResponseEntity<TransactionRecordDTO> withdraw(
                        @PathVariable Long fromCustomerId,
                        @PathVariable("accountId") Long fromAccountId,
                        @Valid @RequestBody DepositWithdrawRequest request) {

                log.info("Withdraw request from customer={}, account={}, amount={}",
                                fromCustomerId, fromAccountId, request.getAmount());

                TransactionRecordDTO transaction = transactionRecordService.performTransaction(
                                fromCustomerId, fromAccountId,
                                null, null,
                                request.getAmount(), "Withdraw");
                return ResponseEntity.ok(transaction);
        }

        @GetMapping
        public ResponseEntity<List<TransactionRecordDTO>> getTransactionHistory(
                        @PathVariable("fromCustomerId") Long customerId,
                        @PathVariable("accountId") Long accountId) {

                log.info("Getting transaction history for customer={}, account={}", customerId, accountId);

                List<TransactionRecordDTO> transactions = transactionRecordService.getTransactionHistory(customerId,
                                accountId);
                return ResponseEntity.ok(transactions);
        }
}
