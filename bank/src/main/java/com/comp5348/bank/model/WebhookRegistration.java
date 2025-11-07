package com.comp5348.bank.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "webhook_registrations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String event;

    @Column(name = "callback_url", nullable = false)
    private String callbackUrl;

    @CreationTimestamp
    @Column(name = "registered_at", nullable = false)
    private LocalDateTime registeredAt;

    public WebhookRegistration(String event, String callbackUrl) {
        this.event = event;
        this.callbackUrl = callbackUrl;
    }
}
