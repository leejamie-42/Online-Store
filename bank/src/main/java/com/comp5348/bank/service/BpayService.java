package com.comp5348.bank.service;

import com.comp5348.bank.dto.BpayRefundResponse;
import com.comp5348.bank.dto.BpayRequest;
import com.comp5348.bank.dto.BpayResponse;
import com.comp5348.bank.dto.TransactionRecordDTO;
import com.comp5348.bank.enums.BpayStatus;
import com.comp5348.bank.exception.BpayNotFoundException;
import com.comp5348.bank.exception.DuplicateBpayException;
import com.comp5348.bank.exception.InvalidBpayStatusException;
import com.comp5348.bank.exception.MerchantNotFoundException;
import com.comp5348.bank.exception.RefundNotAllowedException;
import com.comp5348.bank.exception.TransactionNotFoundException;
import com.comp5348.bank.model.BpayTransactionInformation;
import com.comp5348.bank.model.Merchant;
import com.comp5348.bank.model.TransactionRecord;
import com.comp5348.bank.repository.BpayTransactionRepository;
import com.comp5348.bank.repository.MerchantRepository;
import com.comp5348.bank.repository.TransactionRecordRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BpayService {

    private final BpayTransactionRepository bpayRepository;
    private final MerchantRepository merchantRepository;
    private final TransactionRecordRepository transactionRecordRepository;
    private final TransactionRecordService transactionService;
    private final WebhookService webhookService;

    private static final int EXPIRY_MINUTES = 30;

    /**
     * Generate BPAY payment instructions for Store order
     */
    @Transactional
    public BpayResponse createBpayPayment(BpayRequest request) {
        log.info(
            "Creating BPAY for order {} amount {}",
            request.getOrderId(),
            request.getAmount()
        );

        // Resolve merchant by account id
        Merchant merchant = merchantRepository
            .findByAccountId(request.getAccountId())
            .orElseThrow(() ->
                new MerchantNotFoundException(
                    "Merchant not found for accountId"
                )
            );

        // Generate unique reference
        String referenceId = "BP-" + request.getOrderId();

        // Check for duplicates
        if (bpayRepository.findByReferenceId(referenceId).isPresent()) {
            throw new DuplicateBpayException(
                "BPAY already exists for order: " + request.getOrderId()
            );
        }

        // Create BPAY record
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(
            EXPIRY_MINUTES
        );

        BpayTransactionInformation bpay = BpayTransactionInformation.builder()
            .referenceId(referenceId)
            .billerCode(merchant.getBillerCode())
            .amount(request.getAmount())
            .status(BpayStatus.pending)
            .expiredAt(expiresAt)
            .build();

        bpay = bpayRepository.save(bpay);

        log.info(
            "BPAY created: biller={}, ref={}, amount={}",
            merchant.getBillerCode(),
            referenceId,
            request.getAmount()
        );

        return BpayResponse.builder()
            .billerCode(merchant.getBillerCode())
            .referenceNumber(referenceId)
            .amount(request.getAmount())
            .expiresAt(expiresAt)
            .build();
    }

    /**
     * Process customer payment (triggered via API)
     */
    @Transactional
    public void processBpayPayment(
        String referenceId,
        Long customerId,
        Long customerAccountId
    ) {
        log.info(
            "Processing BPAY payment for reference: {}, customer: {}, account: {}",
            referenceId,
            customerId,
            customerAccountId
        );

        BpayTransactionInformation bpay = bpayRepository
            .findByReferenceId(referenceId)
            .orElseThrow(() ->
                new BpayNotFoundException("BPAY not found: " + referenceId)
            );

        if (bpay.getStatus() != BpayStatus.pending) {
            throw new InvalidBpayStatusException(
                "BPAY already processed: " + bpay.getStatus()
            );
        }

        // Update BPAY status
        bpay.setStatus(BpayStatus.paid);
        bpay.setPaidAt(LocalDateTime.now());
        bpayRepository.save(bpay);

        // Create transaction record (deposit to merchant account)
        Merchant merchant = merchantRepository
            .findByBillerCode(bpay.getBillerCode())
            .orElseThrow(() ->
                new MerchantNotFoundException(
                    "Merchant not found for billerCode"
                )
            );

        TransactionRecordDTO transaction =
            transactionService.performTransaction(
                customerId,
                customerAccountId, // From: customer paying
                merchant.getCustomer().getId(),
                merchant.getAccount().getId(),
                bpay.getAmount(),
                "BPAY payment: " + referenceId
            );

        // Link transaction to BPAY record for refund tracking
        TransactionRecord transactionEntity = transactionRecordRepository
            .findById(transaction.getId())
            .orElseThrow(() ->
                new TransactionNotFoundException(
                    "Transaction not found: " + transaction.getId()
                )
            );
        bpay.setTransactionRecord(transactionEntity);
        bpayRepository.save(bpay);

        // Send webhook to Store
        webhookService.sendPaymentCompletedWebhook(bpay);

        log.info("BPAY payment processed successfully: {}", referenceId);
    }

    /**
     * Request refund for a paid BPAY transaction
     */
    @Transactional
    public BpayRefundResponse requestRefund(String referenceId) {
        log.info("Requesting refund for BPAY reference: {}", referenceId);

        // Fetch BPAY transaction
        BpayTransactionInformation bpay = bpayRepository
            .findByReferenceId(referenceId)
            .orElseThrow(() ->
                new BpayNotFoundException("BPAY not found: " + referenceId)
            );

        // Validate BPAY status - must be paid to refund
        if (bpay.getStatus() != BpayStatus.paid) {
            throw new RefundNotAllowedException(
                "Cannot refund BPAY with status: " +
                    bpay.getStatus() +
                    ". Only paid transactions can be refunded."
            );
        }

        // Validate transaction record exists
        TransactionRecord originalTransaction = bpay.getTransactionRecord();

        if (originalTransaction == null) {
            throw new TransactionNotFoundException(
                "Original transaction not found for BPAY: " + referenceId
            );
        }

        log.info(
            "Original transaction found: id={}, amount={}, from={}, to={}",
            originalTransaction.getId(),
            originalTransaction.getAmount(),
            originalTransaction.getFromAccount().getId(),
            originalTransaction.getToAccount().getId()
        );

        // Update BPAY status to cancelled
        bpay.setStatus(BpayStatus.cancelled);
        bpayRepository.save(bpay);

        // Perform refund transaction (swap from/to accounts)
        TransactionRecordDTO refundTransaction =
            transactionService.performTransaction(
                originalTransaction.getToAccount().getCustomer().getId(), // Merchant returns money
                originalTransaction.getToAccount().getId(),
                originalTransaction.getFromAccount().getCustomer().getId(), // Customer receives money
                originalTransaction.getFromAccount().getId(),
                originalTransaction.getAmount(),
                "Refund for BPAY: " + referenceId
            );

        // Send refund webhook to Store
        webhookService.sendRefundCompletedWebhook(bpay, refundTransaction);

        log.info(
            "BPAY refund completed: reference={}, refundTxId={}, amount={}",
            referenceId,
            refundTransaction.getId(),
            refundTransaction.getAmount()
        );

        return BpayRefundResponse.builder()
            .referenceId(referenceId)
            .refundTransactionId(refundTransaction.getId())
            .amount(refundTransaction.getAmount())
            .status(BpayStatus.cancelled)
            .refundedAt(LocalDateTime.now())
            .build();
    }
}
