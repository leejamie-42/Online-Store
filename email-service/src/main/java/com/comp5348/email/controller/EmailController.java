package com.comp5348.email.controller;

import com.comp5348.email.dto.EmailLogDto;
import com.comp5348.email.entity.EmailLog;
import com.comp5348.email.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

// REST API for email service
// Frontend calls this to show email notification history
@RestController
@RequestMapping("/api/emails")
@CrossOrigin(origins = "*")
public class EmailController {

  private static final Logger log = LoggerFactory.getLogger(EmailController.class);

  private final EmailService emailService;

  public EmailController(EmailService emailService) {
    this.emailService = emailService;
  }

  // Get all emails for an order
  // Frontend shows this in order tracking page
  @GetMapping("/order/{orderId}")
  public ResponseEntity<List<EmailLogDto>> getEmailsForOrder(@PathVariable Long orderId) {
    log.debug("Getting email history for order {}", orderId);

    try {
      List<EmailLog> emails = emailService.getEmailsForOrder(orderId);

      // Convert to DTOs
      List<EmailLogDto> dtos = emails.stream()
          .map(EmailLogDto::new)
          .collect(Collectors.toList());

      return ResponseEntity.ok(dtos);
    } catch (Exception e) {
      log.error("Failed to get emails for order {}: {}", orderId, e.getMessage());
      throw e;
    }
  }

  // Quick health check
  @GetMapping("/health")
  public ResponseEntity<String> health() {
    return ResponseEntity.ok("Email service is running");
  }
}
