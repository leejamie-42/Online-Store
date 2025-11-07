package com.comp5348.email.common.repository;

import com.comp5348.email.common.entity.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Tracks processed messages so we don't handle the same one twice
@Repository
public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, Long> {

  // Check if we've already processed this message ID
  // Prevents duplicate processing when RabbitMQ redelivers messages
  boolean existsByMessageId(String messageId);

}
