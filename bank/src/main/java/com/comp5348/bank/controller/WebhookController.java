package com.comp5348.bank.controller;

import com.comp5348.bank.dto.WebhookRegistrationRequest;
import com.comp5348.bank.service.WebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bank/api/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final WebhookService webhookService;

    /**
     * Register webhook callback URL
     * POST /bank/api/webhooks/register
     */
    @PostMapping("/register")
    public ResponseEntity<Void> registerWebhook(
            @Valid @RequestBody WebhookRegistrationRequest request) {

        log.info("Registering webhook: event={}, url={}", request.getEvent(), request.getCallbackUrl());
        webhookService.registerWebhook(request.getEvent(), request.getCallbackUrl());
        return ResponseEntity.ok().build();
    }
}
