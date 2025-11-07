package com.comp5348.bank.repository;

import com.comp5348.bank.model.WebhookRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebhookRegistrationRepository extends JpaRepository<WebhookRegistration, Long> {
    Optional<WebhookRegistration> findByEvent(String event);
}
