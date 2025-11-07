package com.comp5348.store.config;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ health check indicator for Spring Boot Actuator.
 *
 * This component checks RabbitMQ connection health and exposes it
 * via the /actuator/health endpoint.
 *
 * Health Status:
 * 
 * UP - RabbitMQ connection is active
 * DOWN - RabbitMQ connection failed
 * 
 *
 */
@Component("rabbitmq")
@RequiredArgsConstructor
public class RabbitMQHealthCheck implements HealthIndicator {

  private final ConnectionFactory connectionFactory;

  /**
   * Check RabbitMQ connection health.
   *
   * @return Health status with connection details
   */
  @Override
  public Health health() {
    try {
      // Try to create and close a connection to verify connectivity
      Connection connection = connectionFactory.createConnection();
      connection.close();

      return Health.up()
          .withDetail("rabbitmq", "Available")
          .withDetail("host", connectionFactory.getHost())
          .withDetail("port", connectionFactory.getPort())
          .build();

    } catch (Exception e) {
      return Health.down()
          .withDetail("rabbitmq", "Unavailable")
          .withDetail("error", e.getMessage())
          .build();
    }
  }
}
