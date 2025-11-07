package com.comp5348.deliveryco.repository;

import com.comp5348.deliveryco.entity.WebhookRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WebhookRepository extends JpaRepository<WebhookRegistration, Long> {
  Optional<WebhookRegistration> findByEvent(String event);
  void deleteByEvent(String event);
}
