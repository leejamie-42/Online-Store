package com.comp5348.bank.service;

import com.comp5348.bank.dto.PaymentWebhookPayload;
import com.comp5348.bank.dto.TransactionRecordDTO;
import com.comp5348.bank.model.BpayTransactionInformation;
import com.comp5348.bank.model.WebhookRegistration;
import com.comp5348.bank.repository.WebhookRegistrationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final RestTemplate restTemplate;
    private final WebhookRegistrationRepository webhookRegistrationRepository;

    /**
     * Register webhook callback URL from Store.
     * Store calls this to tell Bank where to send payment notifications.
     *
     * @param event       The event type (e.g., "PAYMENT_EVENT")
     * @param callbackUrl Store's webhook endpoint (e.g.,
     *                    "http://localhost:8081/api/webhooks/payment")
     */
    @Transactional
    public void registerWebhook(String event, String callbackUrl) {
        // Check if webhook already exists for this event
        WebhookRegistration existing = webhookRegistrationRepository
            .findByEvent(event)
            .orElse(null);

        if (existing != null) {
            // Update existing webhook
            existing.setCallbackUrl(callbackUrl);
            webhookRegistrationRepository.save(existing);
            log.info("Webhook updated for event={}: {}", event, callbackUrl);
        } else {
            // Create new webhook registration
            WebhookRegistration registration = new WebhookRegistration(
                event,
                callbackUrl
            );
            webhookRegistrationRepository.save(registration);
            log.info("Webhook registered for event={}: {}", event, callbackUrl);
        }
    }

    public void sendPaymentCompletedWebhook(BpayTransactionInformation bpay) {
        // Fetch the registered webhook URL for PAYMENT_EVENT
        WebhookRegistration registration = webhookRegistrationRepository
            .findByEvent("PAYMENT_EVENT")
            .orElse(null);

        if (registration == null) {
            log.warn(
                "No webhook registered for PAYMENT_EVENT, skipping payment notification for referenceId={}",
                bpay.getReferenceId()
            );
            return;
        }

        String webhookUrl = registration.getCallbackUrl();

        PaymentWebhookPayload payload = PaymentWebhookPayload.builder()
            .type("BPAY_PAYMENT_COMPLETED")
            .orderId(bpay.getReferenceId().replace("BP-", "")) // "BP-ORD-1" → "ORD-1"
            .paymentId(bpay.getReferenceId())
            .amount(bpay.getAmount())
            .paidAt(bpay.getPaidAt())
            .build();

        try {
            log.info(
                "Sending webhook to {} for referenceId={}",
                webhookUrl,
                bpay.getReferenceId()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PaymentWebhookPayload> entity = new HttpEntity<>(
                payload,
                headers
            );

            ResponseEntity<Void> response = restTemplate.exchange(
                webhookUrl,
                HttpMethod.POST,
                entity,
                Void.class
            );

            log.info(
                "Webhook sent successfully to {}: status={}, referenceId={}",
                webhookUrl,
                response.getStatusCode(),
                bpay.getReferenceId()
            );
        } catch (Exception e) {
            log.error(
                "Failed to send webhook to {} for referenceId={}: {}",
                webhookUrl,
                bpay.getReferenceId(),
                e.getMessage()
            );
        }
    }

    public void sendRefundCompletedWebhook(
        BpayTransactionInformation bpay,
        TransactionRecordDTO refundTransaction
    ) {
        // Fetch the registered webhook URL for PAYMENT_EVENT
        WebhookRegistration registration = webhookRegistrationRepository
            .findByEvent("PAYMENT_EVENT")
            .orElse(null);

        if (registration == null) {
            log.warn(
                "No webhook registered for PAYMENT_EVENT, skipping refund notification for referenceId={}",
                bpay.getReferenceId()
            );
            return;
        }

        String webhookUrl = registration.getCallbackUrl();

        PaymentWebhookPayload payload = PaymentWebhookPayload.builder()
            .type("REFUND_COMPLETED")
            .orderId(bpay.getReferenceId().replace("BP-", "")) // "BP-ORD-1" → "ORD-1"
            .paymentId(bpay.getReferenceId())
            .refundId(refundTransaction.getId())
            .amount(bpay.getAmount())
            .refundedAt(refundTransaction.getCreatedAt())
            .build();

        try {
            log.info(
                "Sending refund webhook to {} for referenceId={}",
                webhookUrl,
                bpay.getReferenceId()
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<PaymentWebhookPayload> entity = new HttpEntity<>(
                payload,
                headers
            );

            ResponseEntity<Void> response = restTemplate.exchange(
                webhookUrl,
                HttpMethod.POST,
                entity,
                Void.class
            );

            log.info(
                "Refund webhook sent successfully to {}: status={}, referenceId={}",
                webhookUrl,
                response.getStatusCode(),
                bpay.getReferenceId()
            );
        } catch (Exception e) {
            log.error(
                "Failed to send refund webhook to {} for referenceId={}: {}",
                webhookUrl,
                bpay.getReferenceId(),
                e.getMessage()
            );
        }
    }
}
