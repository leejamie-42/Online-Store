package com.comp5348.store.service.event;

import com.comp5348.store.config.RabbitMQConfig;
import com.comp5348.store.dto.event.EmailEvent;
import com.comp5348.store.dto.event.InventoryRollbackEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventPublisher service.
 */
@ExtendWith(MockitoExtension.class)
class EventPublisherTest {

  @Mock
  private RabbitTemplate rabbitTemplate;

  @InjectMocks
  private EventPublisher eventPublisher;

  @Test
  void publishEmailEvent_Success() {
    // Given
    EmailEvent event = EmailEvent.builder()
        .type("ORDER_CONFIRMATION")
        .to("customer@example.com")
        .template("order_confirmation")
        .params(Map.of("orderId", 123L))
        .eventId("evt-123")
        .timestamp(LocalDateTime.now())
        .build();

    // When
    eventPublisher.publishEmailEvent(event);

    // Then
    verify(rabbitTemplate).convertAndSend(
        eq(RabbitMQConfig.TOPIC_EXCHANGE),
        eq("email.order_confirmation"),
        eq(event));
  }

  @Test
  void publishEmailEvent_HandlesException() {
    // Given
    EmailEvent event = EmailEvent.builder()
        .type("ORDER_CONFIRMATION")
        .to("customer@example.com")
        .build();

    doThrow(new RuntimeException("RabbitMQ connection failed"))
        .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

    // When - should not throw
    eventPublisher.publishEmailEvent(event);

    // Then - exception is caught and logged
    verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
  }

  @Test
  void publishInventoryRollbackEvent_Success() {
    // Given
    InventoryRollbackEvent event = InventoryRollbackEvent.builder()
        .orderId(123L)
        .productId(456L)
        .amount(5)
        .reason("Order cancelled")
        .eventId("evt-rollback-123")
        .timestamp(LocalDateTime.now())
        .build();

    // When
    eventPublisher.publishInventoryRollbackEvent(event);

    // Then
    verify(rabbitTemplate).convertAndSend(
        eq(RabbitMQConfig.TOPIC_EXCHANGE),
        eq("inventory.rollback.request"),
        eq(event));
  }

  @Test
  void publishInventoryRollbackEvent_HandlesException() {
    // Given
    InventoryRollbackEvent event = InventoryRollbackEvent.builder()
        .orderId(123L)
        .reason("Order cancelled")
        .build();

    doThrow(new RuntimeException("RabbitMQ connection failed"))
        .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

    // When - should not throw
    eventPublisher.publishInventoryRollbackEvent(event);

    // Then - exception is caught and logged
    verify(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));
  }
}
