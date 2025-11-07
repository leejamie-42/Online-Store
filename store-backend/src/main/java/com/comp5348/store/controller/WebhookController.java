package com.comp5348.store.controller;

import com.comp5348.store.dto.delivery.DeliveryWebhookEvent;
import com.comp5348.store.dto.payment.PaymentWebhookEvent;
import com.comp5348.store.service.PaymentService;
import com.comp5348.store.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Webhook endpoints for external service callbacks.
 *
 * Security: In production, validate webhook signatures.
 */
@Tag(name = "Webhook", description = "Webhook endpoints for external service callbacks")
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

  private final PaymentService paymentService;
  private final ShipmentService shipmentService;

  /**
   * Receive payment events from Bank Service.
   *
   * POST /api/webhooks/payment
   *
   * Request:
   * {
   *   "type": "BPAY_PAYMENT_COMPLETED",
   *   "order_id": "ORD-001",
   *   "payment_id": "PAY-123",
   *   "amount": 149.97,
   *   "paid_at": "2025-10-21T11:00:00Z"
   * }
   *
   * Response: 200 OK
   */
  @Operation(
      summary = "Handle payment webhook from Bank Service",
      description = "Process payment completion or refund completion events from Bank Service.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid webhook payload"),
        @ApiResponse(responseCode = "404", description = "Payment or refund not found")
      })
  @PostMapping("/payment")
  public ResponseEntity<Void> handlePaymentWebhook(@RequestBody PaymentWebhookEvent event) {
    log.info("Received payment webhook: type={}, orderId={}", event.getType(), event.getOrderId());

    // TODO: In production, validate webhook signature

    try {
      paymentService.handlePaymentWebhook(event);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      log.error("Failed to process payment webhook: {}", e.getMessage());
      // Return 200 to prevent Bank Service retries
      // Failed webhooks should be logged and handled manually
      return ResponseEntity.ok().build();
    }
  }

  /**
   * Receive delivery status events from DeliveryCo Service.
   *
   * POST /api/webhooks/delivery
   *
   * Request:
   * {
   *   "shipmentId": "SHIP-123",
   *   "event": "PICKED_UP",
   *   "timestamp": "2025-10-21T12:00:00Z"
   * }
   *
   * Response: 200 OK
   */
  @Operation(
      summary = "Handle delivery webhook from DeliveryCo Service",
      description = "Process delivery status update events from DeliveryCo Service.")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid webhook payload"),
        @ApiResponse(responseCode = "404", description = "Shipment not found")
      })
  @PostMapping("/delivery")
  public ResponseEntity<Void> handleDeliveryWebhook(@RequestBody DeliveryWebhookEvent event) {
    log.info("Received delivery webhook: shipmentId={}, event={}",
        event.getShipmentId(), event.getEvent());

    // TODO: In production, validate webhook signature

    try {
      shipmentService.handleDeliveryWebhook(event);
      return ResponseEntity.ok().build();
    } catch (Exception e) {
      log.error("Failed to process delivery webhook: {}", e.getMessage());
      // Return 200 to prevent DeliveryCo Service retries
      // Failed webhooks should be logged and handled manually
      return ResponseEntity.ok().build();
    }
  }
}
