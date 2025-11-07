package com.comp5348.store.service.bank;

import com.comp5348.store.dto.payment.*;
import com.comp5348.store.exception.BankServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BankServiceClient {

    private final RestTemplate restTemplate;

    @Value("${bank.service.base-url}")
    private String bankServiceBaseUrl;

    @Value("${bank.service.account-id}")
    private Long storeAccountId;

    /**
     * Generate BPAY payment instructions via Bank Service.
     *
     * @param orderId Order identifier
     * @param amount Payment amount
     * @return BPAY details (biller_code, reference_number, expires_at)
     * @throws BankServiceException if Bank Service fails
     */
    public BankBpayResponse createBpayPayment(String orderId, BigDecimal amount) {
        String url = bankServiceBaseUrl + "/bank/api/bpay";

        BankBpayRequest request = BankBpayRequest.builder()
            .accountId(storeAccountId)
            .orderId(orderId)
            .amount(amount)
            .build();

        try {
            log.info("Creating BPAY payment for order {} with amount {}", orderId, amount);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<BankBpayRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<BankBpayResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                BankBpayResponse.class
            );

            if (response.getStatusCode() != HttpStatus.OK && response.getStatusCode() != HttpStatus.CREATED) {
                throw new BankServiceException("Bank Service returned status: " + response.getStatusCode());
            }

            BankBpayResponse bpayResponse = response.getBody();
            log.info("Successfully created BPAY payment for order {}: biller={}, ref={}",
                orderId, bpayResponse.getBillerCode(), bpayResponse.getReferenceNumber());

            return bpayResponse;

        } catch (RestClientException e) {
            log.error("Failed to create BPAY payment for order {}: {}", orderId, e.getMessage());
            throw new BankServiceException("Failed to communicate with Bank Service", e);
        }
    }

    /**
     * Register webhook callback URL with Bank Service.
     *
     * @param callbackUrl Store's webhook endpoint
     */
    public void registerWebhook(String callbackUrl) {
        String url = bankServiceBaseUrl + "/bank/api/webhooks/register";

        try {
            log.info("Registering payment webhook: {}", callbackUrl);

            Map<String, String> request = Map.of(
                "event", "PAYMENT_EVENT",
                "callback_url", callbackUrl
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

            restTemplate.exchange(url, HttpMethod.POST, entity, Void.class);

            log.info("Successfully registered payment webhook");

        } catch (RestClientException e) {
            log.error("Failed to register payment webhook: {}", e.getMessage());
            // Don't throw - webhook registration failure shouldn't prevent app startup
        }
    }

    /**
     * Request refund from Bank Service.
     *
     * @param paymentId Payment identifier
     * @param amount Refund amount
     * @param reason Refund reason
     * @return Transaction ID from Bank
     */
    public Long requestRefund(String paymentId, BigDecimal amount, String reason) {
        String url = bankServiceBaseUrl + "/bank/api/refunds";

        try {
            log.info("Requesting refund for payment {} with amount {}", paymentId, amount);

            Map<String, Object> request = Map.of(
                "payment_id", paymentId,
                "amount", amount,
                "reason", reason
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
            );

            Map<String, Object> body = response.getBody();
            Long transactionId = ((Number) body.get("transaction_id")).longValue();

            log.info("Successfully requested refund for payment {}: transaction_id={}",
                paymentId, transactionId);

            return transactionId;

        } catch (RestClientException e) {
            log.error("Failed to request refund for payment {}: {}", paymentId, e.getMessage());
            throw new BankServiceException("Failed to request refund from Bank Service", e);
        }
    }
}
