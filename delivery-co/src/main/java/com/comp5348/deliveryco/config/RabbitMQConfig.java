package com.comp5348.deliveryco.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

// RabbitMQ setup for DeliveryCo service
// Also includes RestTemplate for HTTP webhook calls to Store
@Configuration
public class RabbitMQConfig {

  // Topic exchange name - routes messages based on routing key patterns
  public static final String EXCHANGE_NAME = "store.events";

  // Queue for email notifications
  // Email service will consume messages from here
  public static final String EMAIL_QUEUE = "email.notifications";

  // Routing pattern for delivery events
  // Matches "delivery.started", "delivery.completed" etc
  public static final String DELIVERY_ROUTING_KEY = "delivery.*";

  // Creates topic exchange for routing events
  // Durable = survives restarts
  @Bean
  public TopicExchange exchange() {
    return new TopicExchange(EXCHANGE_NAME, true, false);
  }

  // Creates email notification queue
  // Durable and not exclusive
  @Bean
  public Queue emailQueue() {
    return new Queue(EMAIL_QUEUE, true, false, false);
  }

  // Binds queue to exchange with "delivery.*" pattern
  // Any delivery event goes to email queue
  @Bean
  public Binding emailBinding() {
    return BindingBuilder
        .bind(emailQueue())
        .to(exchange())
        .with(DELIVERY_ROUTING_KEY);
  }

  // JSON converter for messages
  // Converts Java objects to/from JSON automatically
  @Bean
  public MessageConverter jsonMessageConverter() {
    return new Jackson2JsonMessageConverter();
  }

  // RabbitTemplate for sending messages
  // Uses JSON converter for all messages
  @Bean
  public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    RabbitTemplate template = new RabbitTemplate(connectionFactory);
    template.setMessageConverter(jsonMessageConverter());
    return template;
  }

  // RestTemplate for making HTTP calls to Store's webhook endpoint
  // Used to notify Store when shipment status changes
  @Bean
  public RestTemplate restTemplate() {
    return new RestTemplate();
  }

}
