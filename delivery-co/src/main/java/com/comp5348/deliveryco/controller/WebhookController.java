package com.comp5348.deliveryco.controller;

import com.comp5348.deliveryco.entity.WebhookRegistration;
import com.comp5348.deliveryco.repository.WebhookRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

/**
 * Webhook registration endpoint for Store to register callback URLs
 */
@RestController
@RequestMapping("/deliveryCo/api/webhooks")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

  private final WebhookRepository webhookRepository;

  /**
   * Register webhook callback URL from Store
   *
   * POST /deliveryCo/api/webhooks/register
   *
   * Request:
   * {
   *   "event": "SHIPMENT_STATUS_UPDATE",
   *   "callback_url": "http://localhost:8080/api/webhooks/delivery"
   * }
   *
   * Response: 200 OK
   */
  @PostMapping("/register")
  @Transactional
  public ResponseEntity<Void> registerWebhook(@RequestBody WebhookRequest request) {
    log.info("Registering webhook: event={}, callbackUrl={}",
        request.getEvent(), request.getCallback_url());

    // Delete existing registration (prevent duplicates)
    webhookRepository.deleteByEvent(request.getEvent());

    // Create new registration
    WebhookRegistration registration = WebhookRegistration.builder()
        .event(request.getEvent())
        .callbackUrl(request.getCallback_url())
        .build();

    webhookRepository.save(registration);

    log.info("Webhook registered successfully: {}", request.getEvent());
    return ResponseEntity.ok().build();
  }

  /**
   * DTO for webhook registration request
   * Matches William's Store Backend format (callback_url with underscore)
   */
  @Data
  public static class WebhookRequest {
    private String event;
    private String callback_url;  // Underscore format to match William's code
  }
}
